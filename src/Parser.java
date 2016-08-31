import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

// парсер (містить по 1 процедурі
// (основаній на граматиці із вилученою лівою рекурсією
// із початкової граматики) для кожного нетермінала)

public class Parser {
    private Token look;   // попередній перегляд
    HashMap<Token, Id> table = new HashMap<Token, Id>(); // таблиця
    static int label = 0; // номер поточної мітки
    public Lexer lex;    // лексичний аналізатор
    public FileWriter out;
    String code = "MODEL SMALL\nSTACK 100h\n\nDATASEG\n";

    public Parser(String inFile, String outFile) throws IOException {
        lex = new Lexer(inFile);
        out = new FileWriter(outFile);
        move();
    }

    void move() throws IOException { look = lex.scan(); } // сканування чергового символа

    void error(String s) { throw new Error("near line "+Lexer.line+": "+s);  }

    void match(int t) throws IOException { // перевірка чи сканований символ - t
        if( look.tag == t ) move();        // якщо так, сканування наступного символа
        else { error("syntax error");      // якщо ні, вивід про помилку
            System.exit(0); }
    }

    public void program() throws IOException {
        match('{');
        decls();
        code += "ENDS\n\nCODESEG\nMain:\n\tMOV AX, @data\n\tMOV DS, AX\n\n\t";
        while ( look.tag != '}' ) stmt();
        match('}');
        out.write(code+"\n\tMOV AX,4C00h\n\tINT 21h\nEND Main\nEND"); // вивід асемблерного коду
    }

    void decls() throws IOException {
        while( look.tag == Tag.BASIC ) {
            Id id = new Id();
            id.type = look.toString();
            match(Tag.BASIC);
            id.lexeme = look.toString();
            if (table.get(look) != null) { error(look.toString() + " is already declarated!");}
            table.put( look, id );
            code += "\t" + look.toString() + " DW ";
            match(Tag.ID);
            match('=');
            code += look.toString() + "\n";
            match(Tag.NUM);
            match(';');
        }
    }

    void block() throws IOException {
        match('{');
        while ( look.tag != '}' ) stmt();
        match('}');
    }

    void stmt() throws IOException {
        switch( look.tag ) {
            case ';':
                move(); break;
            case '{':
                block(); break;
            case Tag.IF:
                ifConstr(); break;
            case Tag.FOR:
                forConstr(); break;
            default:
                rightConstr();
        }
    }

    void ifConstr() throws IOException {
        match(Tag.IF);
        match('(');
        logicExpr();
        match(')');
        stmt();
        if( look.tag == Tag.ELSE ) {
            code += "JMP L"+(label+2)+"\n\t\nL"+(++label)+":\n\t";
            move();
            stmt();
        }
        code += "\nL"+ (++label) + ":\n\t";
    }

    void forConstr() throws IOException {
        int forLabel = ++label;
        int afterForLabel = ++label;
        match(Tag.FOR);
        String iterator = look.toString();
        match(Tag.ID);
        match('=');
        code += "MOV "+iterator+", "+look.toString()+"\nL"+forLabel+":\n\t";
        match(Tag.NUM);
        match(Tag.TO);
        code += "MOV AX, "+iterator+"\n\tCMP AX, "+look.toString()+"\n\tJNLE L"+afterForLabel+"\n\t";
        match(Tag.NUM);
        block();
        code += "INC "+iterator+"\n\tJMP L"+forLabel+"\nL"+afterForLabel+":\n\t";
    }


    void rightConstr() throws IOException {
        Id id = table.get(look);
        if( id == null ) error(look.toString() + " undeclared");
        if ( id.type.equals("const") ) {
            error(look.toString() + " is const. You can't redefine it!");
        }
        match(Tag.ID);
        if ( look.tag == Tag.DEC ) {
            code += "DEC "+ id.lexeme + "\n\t";
            move(); return;
        } else {
            match('=');
            arithmeticExpr(id);
            code += "MOV "+ id + ", AX\n\t";
            match(';');
        }
        if ( look.tag == Tag.INC ) {
            code += "INC "+ id.lexeme + "\n\t";
            move(); return;
        } else {
            match('=');
            arithmeticExpr(id);
            code += "MOV "+ id + ", AX\n\t";
            match(';');
        }
    }

    void arithmeticExpr(Id id) throws IOException {
        String op1 = "", op2 = "", op = "";
        boolean bracket = false;
        op1 = leftArithmeticExpr(id, op1);
        rightArithmeticExpr(id, op, op1, op2, bracket);
    }

    String leftArithmeticExpr(Id id, String op1) throws IOException {
        boolean constant = false;
        Id lookId = table.get(look);
        if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
            if (look.tag == Tag.ID ) {
                if( lookId == null ) error(look.toString() + " undeclared");
                if(lookId.type.equals("const")) constant=true;
            }
            op1 = look.toString(); move();
        } else if ( look.tag == Tag.BASIC ) {
            error("syntax error. You can't declare variable here! I'm tired of your obtusity. Goodbuy, loser!");
            System.exit(0);
        } else error("syntax error. Expected number, variable or constant");
        if ( look.tag == Tag.DEC ) {
            if(constant) error(lookId.toString() + " is const. You can't redefine it!");
            op1 = id.lexeme; code += "DEC "+ op1 + "\n\t"; move();
        } code += "MOV AX, "+ op1 + "\n\t";
        return op1;
    }

    void rightArithmeticExpr(Id id, String op, String op1, String op2, boolean bracket) throws IOException {
        while( look.tag == '+' || look.tag == '-' ) {
            switch( look.tag ) {
                case '+':
                    op = "+"; move(); break;
                case '-':
                    op = "-"; move(); break;
            }
            boolean constant = false;
            Id lookId = table.get(look);
            if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
                if (look.tag == Tag.ID ) {
                    if( lookId == null ) error(look.toString() + " undeclared");
                    if(lookId.type.equals("const")) constant=true;
                }
                op2 = look.toString(); move();
            } else if ( look.tag == '(' ) {
                op = brackets(op, id); bracket = true;
            }  else if ( look.tag == Tag.BASIC ) {
                error("syntax error. You can't declare variable here!");
            } else error("syntax error. Expected bracket, number, variable or constant.");
            if ( look.tag == Tag.DEC ) {
                if(constant) error(lookId.toString() + " is const. You can't redefine it!");
                op1 = id.lexeme; code += "DEC "+ op1 + "\n\t"; move();
            }
            if ( look.tag == Tag.INC ) {
                if(constant) error(lookId.toString() + " is const. You can't redefine it!");
                op2 = id.lexeme; code += "INC "+ op2 + "\n\t"; move();
            }
            switch( op ) {
                case "+":
                    if (bracket == false)
                        code += "MOV BX, "+ op2 + "\n\t";
                    else bracket = false;
                    code += "ADD AX, BX\n\t"; break;
                case "-":
                    if (bracket == false)
                        code += "MOV BX, "+ op2 + "\n\t";
                    else bracket = false;
                    code += "SUB AX, BX\n\t"; break;
            }
        }
    }

    String brackets(String op, Id id) throws IOException {
        String op1 = "", op2 = "", oldOp = op;
        boolean bracket = false;
        code += "PUSH AX\n\t"; move();
        op1 = leftArithmeticExpr(id, op1);
        rightArithmeticExpr(id, op, op1, op2, bracket);
        while ( look.tag != ')' ) {
            rightArithmeticExpr(id, op, op1, op2, bracket);
        } move(); code += "MOV BX, AX\n\tPOP AX\n\t"; return oldOp;
    }

    void logicExpr() throws IOException {
        compareExpr();
        while( look.tag == Tag.OR || look.tag == Tag.AND ) {
            if ( look.tag == Tag.OR ) {
                code += "\n\tJMP L"+(label+2)+"\nL"+ (++label) + ":\n\t";
                move(); ++label; compareExpr();
                code += "\nL"+ label + ":\n\t";
            } else {
                code += "\n\t";
                move(); compareExpr();
            }
        }
    }

    void compareExpr() throws IOException {
        String op1 = "", op2 = "";
        String op = "";
        boolean not = false;
        if ( look.tag == '!' ) { not = true; move(); }
        if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
            if (look.tag == Tag.ID ) {
                Id lookId = table.get(look);
                if( lookId == null ) error(look.toString() + " undeclared");
            }
            op1 = look.toString(); move();
        } else error("syntax error. Expected number, variable or constant");
        switch( look.tag ) {
            case '>':
                if(not) op = "<="; else op = ">";
                move(); break;
            case '<':
                if(not) op = ">="; else op = "<";
                move(); break;
            case Tag.EQ:
                if(not) op = "!="; else op = "==";
                move(); break;
            default:
                error("syntax error. Expected compare operation");
        }
        if ( look.tag == Tag.NUM || look.tag == Tag.ID ) {
            if (look.tag == Tag.ID ) {
                Id lookId = table.get(look);
                if( lookId == null ) error(look.toString() + " undeclared");
            }
            op2 = look.toString(); move();
        } else error("syntax error. Expected number, variable or constant");
        code += "MOV AX, " + op1 + "\n\tCMP AX, " + op2 + "\n\t";
        switch( op ) {
            case ">":
                code += "JNG L" + (label+1) + "\n\t"; break;
            case "<":
                code += "JNL L" + (label+1) + "\n\t"; break;
            case "==":
                code += "JNE L" + (label+1) + "\n\t"; break;
            case "!=":
                code += "JE L" + (label+1) + "\n\t"; break;
            case ">=":
                code += "JNGE L" + (label+1) + "\n\t"; break;
            case "<=":
                code += "JNLE L" + (label+1) + "\n\t"; break;
        }
    }
}

