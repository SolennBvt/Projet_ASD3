import java.awt.*;

public class Main {

    public static void main( String[] args )
    {

        try {

            // if (args.length!=1) throw new IllegalArgumentException("Préciser le nom (et chemin) d'un unique fichier png en paramètre");

            ImagePNG png = new ImagePNG("pngs/16.png");
            Quadtree q1 = new Quadtree(png);

        } catch (Exception e) {

            e.printStackTrace();
        }



    }
}
