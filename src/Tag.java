//визначає константи для токенів
public class Tag {
    public final static int
            // список конструкцій вхідної мови
            // які складаються більше, ніж з 1 символа (0-255)
            INC   = 267,              // ++
            BASIC = 256,              // базові типи
            ID    = 257,              // ідентифікатори
            NUM   = 258,              // числа
            FOR   = 259, TO   = 260,     // оператор циклу (for to)
            IF    = 261, ELSE  = 262, // умовний оператор (if else)
            EQ    = 263,              // порівняння (==)
            AND   = 264, OR    = 265, // булеві І (&&) та АБО (||)
            DEC   = 266; // декремент (--)
}
