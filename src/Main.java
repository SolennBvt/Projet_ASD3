import java.awt.*;

public class Main {

    public static void main( String[] args )
    {

        try {

            // if (args.length!=1) throw new IllegalArgumentException("Préciser le nom (et chemin) d'un unique fichier png en paramètre");

            ImagePNG png = new ImagePNG("pngs/32-tux.png");
            Quadtree q1 = new Quadtree(png);
            Quadtree q2 = new Quadtree(png);
            q1.compressDelta(30);
            System.out.println("compression avec degradation delta 30 :\n"+q1.toString());
            System.out.println("avant compression phi, taille : "+q2.getSize());
            q2.compressPhi(10);
            System.out.println("compression avec degradation phi 10 :\n"+q2.toString());
            System.out.println("apres compression phi, taille : "+q2.getSize());

        } catch (Exception e) {

            e.printStackTrace();
        }



    }
}
