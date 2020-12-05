import java.awt.*;

public class Main {

    public static Color col(String colorStr) {

        return new Color(

                Integer.valueOf( colorStr.substring( 0, 2 ), 16 ),
                Integer.valueOf( colorStr.substring( 2, 4 ), 16 ),
                Integer.valueOf( colorStr.substring( 4, 6 ), 16 ) );
    }

    public static void main(String[] args) {

        try {
/*
            AVL<Integer> a = new AVL<Integer>();
            a.insert(4);
            a.insert(3);
            a.insert(1);
            a.insert(6);
            a.insert(5);
            a.insert(7);

            System.out.println(a.toString() + "----------------->" + a.bal);
            a.remove(4);
            System.out.println(a.toString() + "----------------->" + a.bal);
            a.remove(1);
            System.out.println(a.toString() + "----------------->" + a.bal);
            a.remove(3);
            System.out.println(a.toString() + "----------------->" + a.bal);
*/


            ImagePNG png = new ImagePNG("pngs/1024-cube.png");
            //Quadtree q = new Quadtree(png, 0, 0, png.height(), null);
            Quadtree q = new Quadtree(png, 0, 0, png.height(),null);

            long lStartTime = System.nanoTime();
            q.compressPhi(1);
            long lEndTime = System.nanoTime();

            System.out.println("post phi");
            System.out.println(q.toString());

            png = q.toPNG();
            png.save("resultats/16-phi4.png");

            long output = lEndTime - lStartTime;
            System.out.println("Elapsed time in milliseconds: " + output/1000000);


            /*
            Color f1 = col("ffffff");
            Color f2 = col("000000");
            Color f3 = col("000000");
            Color f4 = col("ffffff");

            double rm, gm, bm;
            double lambda=0, l1, l2, l3, l4;

            rm = (f1.getRed() + f2.getRed() +f3.getRed() +f4.getRed())/4;
            gm = (f1.getGreen() + f2.getGreen() +f3.getGreen() +f4.getGreen())/4;
            bm = (f1.getBlue() + f2.getBlue() +f3.getBlue() +f4.getBlue())/4;
            Color c = new Color((int)rm, (int)gm, (int)bm);

            l1 = Math.sqrt((Math.pow(f1.getRed() - rm, 2) + Math.pow(f1.getGreen() - gm, 2) + Math.pow(f1.getBlue() - bm, 2)) /3);
            l2 = Math.sqrt((Math.pow(f2.getRed() - rm, 2) + Math.pow(f2.getGreen() - gm, 2) + Math.pow(f2.getBlue() - bm, 2)) /3);
            l3 = Math.sqrt((Math.pow(f3.getRed() - rm, 2) + Math.pow(f3.getGreen() - gm, 2) + Math.pow(f3.getBlue() - bm, 2)) /3);
            l4 = Math.sqrt((Math.pow(f4.getRed() - rm, 2) + Math.pow(f4.getGreen() - gm, 2) + Math.pow(f4.getBlue() - bm, 2)) /3);
            lambda = Math.max(l1,l2);
            lambda = Math.max(lambda, l3);
            lambda = Math.max(lambda, l4);
            System.out.println("lambda : " + lambda);
            System.out.println("col : " + ImagePNG.colorToHex(c));
            System.out.println(rm);
            System.out.println(gm);
            System.out.println(bm);
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
