import java.awt.GraphicsEnvironment;

public class ListFonts {
    public static void main(String[] args) {
        String[] fontNames = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        
        for (String name : fontNames) {
            System.out.println(name);
        }
    }
}