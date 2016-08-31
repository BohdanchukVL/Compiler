import java.io.*;
import java.util.HashMap;

// лексичний аналіз
public class Lexer {
    public static int line = 1;
    char peek = ' ';
    HashMap<String, Word> words = new HashMap<String, Word>();
    public BufferedReader in;

    // метод запису ідентифікатора в таблицю символів
    void reserve(Word w) { words.put(w.lexeme, w); }

    public Lexer(String inFile) {

        // спроба відкрити файл із сирцевим кодом програми
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inFile))));;
        }
        // якщо файл не існує
        catch( FileNotFoundException ex ) {
            // вивід повідомлення, що файл не знайдено
            System.out.println("File "+inFile+" not found!");
            // завершення програми
            System.exit(0);
        }

        // запис ключових слів вхідної мови програмування
        // до таблиці символів
        reserve( new Word("if",    Tag.IF)    );
        reserve( new Word("else",  Tag.ELSE)  );
        reserve( new Word("for",    Tag.FOR) );
        reserve( new Word("to",    Tag.TO) );
        reserve( Word.Var ); reserve( Word.Const );

    }

    // метод для зчитування одного символа
    void readch() throws IOException { peek = (char)in.read(); }

    // метод перевірки чи символ, що зчитується
    // ідентичний заданому символу с
    boolean readch(char c) throws IOException {
        readch();
        if( peek != c ) return false;
        peek = ' ';
        return true;
    }

    // метод сканування одного символа (із передуванням за потреби)
    public Token scan() throws IOException {
        for( ; ; readch() ) {
            // пропускаємо пробіли, символи табуляції
            // і символ нового рядка

            if( peek == ' ' || peek == '\t' || peek == '\r' ) continue;
            else if( peek == '\n' ) line++;
                // пропускаємо коментарі
            else if( peek == '/' ) {
                if( readch('*') ) {
                    while(true) {
                        if( peek == '*' ) {
                            readch();
                            if( peek == '/' ) break;
                        }
                        readch();
                    }
                    line ++;
                }
            }
            else break;
        }

        // перевіряємо чи зустрілася деяка конструкція мови
        // котра складається із 2-ох символів

        switch( peek ) {
            case '&':
                if( readch('&') ) return Word.and;  else return new Token('&');
            case '|':
                if( readch('|') ) return Word.or;   else return new Token('|');
            case '=':
                if( readch('=') ) return Word.eq;   else return new Token('=');
            case '-':
                if( readch('-') ) return Word.dec;  else return new Token('-');
            case '+':
                if ( readch('+')) return Word.inc; else return new Token('+');
        }

        // якщо зустрічається число
        if( Character.isDigit(peek) ) {
            int v = 0;
            do { v = 10*v + Character.digit(peek, 10); readch();} while( Character.isDigit(peek) );
            return new Num(v);
        }

        // якщо зустрічається ідентифікатор
        if( Character.isLetter(peek) ) {
            StringBuffer b = new StringBuffer();
            do { b.append(peek); readch(); } while( Character.isLetterOrDigit(peek) );
            String s = b.toString();
            // перевірка чи ідентифікатор не є значенням константи
            if ( s.charAt(0) == 'b' ) {
                boolean isOct = true;
                for (int i=1; i < s.length(); i++) {
                    if ( s.charAt(i) != '0' && s.charAt(i) != '1' && s.charAt(i) != '2' && s.charAt(i) != '3' &&
                            s.charAt(i) != '4' && s.charAt(i) != '5' && s.charAt(i) != '6' && s.charAt(i) != '7') {
                        isOct = false; break;
                    }
                }
                if ( isOct == true ) {
                    return new Num(Integer.parseInt(s.substring(1), 2));
                }
            }
            // перевірка чи ідентифікатор не є зарезервованим словом
            // або ідентифікатором, який уже є у таблиці

            Word w = (Word)words.get(s);
            if( w != null ) return w;
            w = new Word(s, Tag.ID);
            words.put(s, w);
            return w;
        }

        // інакше повертаємо новий токен (1 символ)
        Token tok = new Token(peek); peek = ' ';
        return tok;
    }
}
