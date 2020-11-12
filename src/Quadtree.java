import java.awt.*;
import java.util.ArrayList;


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

    public void setColor(Color value){this.color = value;}

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

    private boolean compress(){ //si au moins 2x2 px -> faudrait tester dans l'appel de cette méthode dans le constructeur pr éviter l'erreur si une img de 1px!!

        if(childs[0].color != null && childs[1].color != null && childs[2].color != null && childs[3].color != null){

            boolean isSameColor = true;
            int i = 1;
            Color comp = childs[0].color;

            while(i < 4 && isSameColor){
                isSameColor = (comp.getRGB() == childs[i].color.getRGB());
                i++;
            }

            if(isSameColor) {
                color = comp;
                for (int j = 0; j < 4; j++) {
                    childs[j] = null;
                }
                return true;
            }

        } else {

            for(int i = 0; i < 4; i++){
                if(childs[i].color == null){
                    if(childs[i].compress()){
                        return compress();
                    }
                }
            }
        }
        return false ;
    }


    public boolean compressDelta(double delta) {

        if(childs[0].color != null && childs[1].color != null && childs[2].color != null && childs[3].color != null){
            float rm = 0, vm = 0, bm = 0;
            boolean isCompressAble = true ;

            for(int i = 0 ; i < 4 ; ++i){
                rm += childs[i].color.getRed();
                vm += childs[i].color.getGreen();
                bm += childs[i].color.getBlue();
            }

            rm = rm/4;
            vm = vm/4;
            bm = bm/4;

            for(int i = 0 ; i < 4 && isCompressAble ; ++i){
                if(Math.sqrt((Math.pow(childs[i].color.getRed()-rm,2)+Math.pow(childs[i].color.getGreen()-rm,2)+Math.pow(childs[i].color.getBlue()-rm,2))/3) > delta){
                    isCompressAble = false ;
                }
            }

            if (isCompressAble) {
                color = new Color((int)rm,(int)vm,(int)bm);
                for (int j = 0; j < 4; j++) {
                    childs[j] = null;
                }
                return true ;
            }

        } else {

            for(int i = 0; i < 4; i++){
                if(childs[i].color == null){
                    if(childs[i].compressDelta(delta)){
                        return compressDelta(delta);
                    }
                }
            }
        }
        return false ;
    }

    private class PhiStorage{
        public float delta ;
        public Quadtree target;
        public PhiStorage father ;
        public Color moyColor ;
        public int childNumber ;

        public PhiStorage(float delta, Quadtree target, PhiStorage father, Color moyColor, int childNumber){
            this.delta = delta ;
            this.target = target ;
            this.father = father ;
            this.moyColor = moyColor ;
            this.childNumber = childNumber ;
        }
    }

    public void compressPhi(int phi) {
        ArrayList<PhiStorage> deltas = new ArrayList<PhiStorage>();
        compressPhi(deltas, null);

        while(deltas.size() > phi){

            float minDelta = deltas.get(0).delta ;
            int minIndex = 0 ;
            for(int i = 1 ; i < deltas.size() ; ++i){
                if(deltas.get(i).delta < minDelta && deltas.get(i).childNumber == 0){
                    minDelta = deltas.get(i).delta;
                    minIndex = i ;
                }
            }

            System.out.println("suppr : "+deltas.get(minIndex).father.childNumber);

            for(int i = 0 ; i < 4 ; ++i) {
                deltas.get(minIndex).target.childs[i] = null;
            }
            deltas.get(minIndex).target.setColor(deltas.get(minIndex).moyColor) ;

            System.out.println(deltas.get(minIndex).target.color);

            if(deltas.get(minIndex).father != null){
                deltas.get(minIndex).father.childNumber -- ;
                if(deltas.get(minIndex).father.childNumber <= 0){

                    System.out.println("try add");
                    System.out.println(deltas.get(minIndex).father.target.toString());
                    System.out.println(deltas.get(minIndex).target.toString());
                    System.out.println("------");

                    deltas.get(minIndex).father.target.compressPhi(deltas,deltas.get(minIndex).father.father);
                }
            }
            deltas.remove(minIndex);

        }

    }

    private Color compressPhi(ArrayList<PhiStorage> deltas, PhiStorage father) {

        float rm = 0, vm = 0, bm = 0;
        float maxDelta = 0 ;
        int childNumber = 0 ;
        Color temp = null;
        PhiStorage thisStorage = new PhiStorage(0,this, father,null, 0);

        System.out.println("test");
        System.out.println(thisStorage.target.toString());

        System.out.println(childs[0].color);
        System.out.println(childs[1].color);
        System.out.println(childs[2].color);
        System.out.println(childs[3].color);
        System.out.println("------");

        for(int i = 0 ; i < 4 ; ++i) {

            if (childs[i].color != null) {
                temp = childs[i].color ;
            } else {
                childNumber ++;
                temp = childs[i].compressPhi(deltas, thisStorage);
            }

            rm += temp.getRed();
            vm += temp.getGreen();
            bm += temp.getBlue();

        }

        rm = rm/4;
        vm = vm/4;
        bm = bm/4;

        for(int i = 0 ; i < 4 ; ++i){
            maxDelta = Math.max((float)Math.sqrt((Math.pow(temp.getRed()-rm,2)+Math.pow(temp.getGreen()-rm,2)+Math.pow(temp.getBlue()-rm,2))/3),maxDelta);
        }
        thisStorage.delta = maxDelta ;
        thisStorage.moyColor = new Color((int)rm,(int)vm,(int)bm) ;
        thisStorage.childNumber = childNumber ;

        deltas.add(thisStorage);

        return thisStorage.moyColor ;
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
