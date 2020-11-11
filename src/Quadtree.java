import java.awt.*;

public class Quadtree {

    //attributs
    private Quadtree childs[] = new Quadtree[4];
    private Color color ;
    private ImagePNG img;

    //constructeurs
    public Quadtree(ImagePNG img){
        System.out.println("//Quadtree(img)//");

        this.img = img;

        int n = log(img.width(),2); //taille de l'arbre
        System.out.println("taille de l'arbre : " + n);

        if(n == 0){
            System.out.println("taille de l'arbre = 0 ");
            this.color = img.getPixel(0,0);
            System.out.println("1 seul pixel : " + color);

        } else {

            //(x,y) milieu de l'image
            int x = (int) Math.pow(2, n-1);
            int y = x;
            System.out.println("milieu : (" + x + ", " + y + ")" );

            //l'écart (le "pas") entre le milieu de la grande case et celui de la sous-case
            int pas = (int) Math.pow(2, n-2);
            System.out.println("pas : " + pas);

            //construction des sous-quadtree
            System.out.println("construction des sous-quadtree");
            subQuadtree(img, x, y, pas);

            //compression sans dégradation du quadtree obtenu
            System.out.println("Avant compression : ");
            System.out.println(toString());
            compress();
            System.out.println("Après compression sans dégradation : ");
            System.out.println(toString());
        }
    }

    private Quadtree(ImagePNG img, Color color){
        System.out.println("//Quadtree(img , color)//");
        this.img = img;
        this.color = color;
    }

    private Quadtree(ImagePNG img, int x, int y, int pas){
        System.out.println("//Quadtree(img, x, y, pas)//");
        subQuadtree(img, x, y, pas);
    }

    //accesseurs
    public Color getColor(){
        return this.color;
    }

    //méthodes
    private void subQuadtree(ImagePNG img, int x, int y, int pas){
        System.out.println("//subQuadtree//");

        if(pas < 1){
            //création de 4 feuilles
            System.out.println("Création de 4 feuilles");
            int matrice[][] = {{-1,-1},{0,-1},{0,0},{-1,0}};
            for(int i = 0 ; i < childs.length ; i++){
                System.out.println("Feuille " + i);
                childs[i] = new Quadtree(img, img.getPixel(x + matrice[i][0], y + matrice[i][1]));
                System.out.println(i + " : " + img.getPixel(x + matrice[i][0], y + matrice[i][1]));
            }
        } else {
            //création de 4 noeuds
            System.out.println("Création de 4 noeuds");
            int matrice[][] = {{-1,-1},{1,-1},{1,1},{-1,1}};
            for(int i = 0 ; i < childs.length ; i++){
                System.out.println("Noeud " + i);
                childs[i] = new Quadtree(img, x + matrice[i][0] * pas, y + matrice[i][1] * pas, pas/2);
            }
        }
    }

    private void compress(){ //si au moins 2x2 px -> faudrait tester dans l'appel de cette méthode dans le constructeur pr éviter l'erreur si une img de 1px!!

        if(childs[0].color != null){

            boolean isSameColor = true;
            int i = 1;
            Color comp = childs[0].color;

            while(i < 4 && isSameColor){
                Color temp = childs[i].color;
                isSameColor = (comp.getRGB() == temp.getRGB());
                i++;
            }

            if(isSameColor){
                color = comp;
                for(int j = 0; j < 4; j++){
                    childs[j] = null;
                }
            }

        } else {

            for(int i = 0; i < 4; i++){
                childs[i].compress();
            }
            compress();
        }
    }

    public String toString(){

        if(childs[0] == null){
            return img.colorToHex(color);
        } else {
            return "( " + childs[0].toString() + " " + childs[1].toString() + " " + childs[2].toString() + " " + childs[3].toString() + " )";
        }
    }

    /*
    private Quadtree compress(Quadtree qd_tree){    }
    public ? compressDelta(Quadtree qd_tree){}
    public ? compressPhi(Quadtree qd_tree){}
    public ImagePNG toPNG(Quadtree qd_tree){}
    public ? toString(){
        //ImagePNG.colorToHex pr obtenir l'hex de la couleur


    }
    */




    /* MAEL
    //constructeurs
    public Quadtree(ImagePNG img){
        int k = log(img.width(),2)+1;
        int matrice[][] = {{0,0},{1,0},{1,1},{0,1}};
        for(int i = 0 ; i < childs.length ; ++i){
            childs[i] = new Quadtree(img,matrice[i][0]*img.width()/2,matrice[i][1]*img.height()/2,4);
        }
    }

    private Quadtree(ImagePNG img,int x, int y,int j){
        if(j == img.width()){
            for(int i = 0 ; i < 4 ; ++i){
                childs[i] = null ;
            }
            color = img.getPixel(x,y);
        }else{
            int matrice[][] = {{0,0},{1,0},{1,1},{0,1}};
            for(int i = 0 ; i < childs.length ; ++i){
                childs[i] = new Quadtree(img,x+((matrice[i][0]*img.width())/j),y+((matrice[i][1]*img.height())/j),j*2);
            }
        }
    }

    //accesseurs
    public Quadtree getChild(int value){
        return childs[value];
    }
*/
    //library
    public static int log(int x, int b){
        return (int) (Math.log(x) / Math.log(b));
    }


}
