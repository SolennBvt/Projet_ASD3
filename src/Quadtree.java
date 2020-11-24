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

    private class PhiPair{
        public double delta ;
        public Quadtree target;
        public Color compressedColor;

        public PhiPair(double delta, Quadtree target, Color compressedColor){
            this.delta = delta ;
            this.target = target ;
            this.compressedColor = compressedColor ;
        }
    }

    public void compressPhi(int phi) {

        ArrayList<PhiPair> pairs = new ArrayList<PhiPair>();
        
        compressPhi(pairs);

        while(getSize() > phi){ //parcour total de l'arbre a chaque getSize !

            pairs.get(0).target.phiCompressThis(pairs.get(0).compressedColor);
            pairs.remove(0);

        }


    }

    private PhiPair compressPhi(ArrayList<PhiPair> pairs){

        PhiPair newPair ;
        Color temp[] = new Color[4] ;
        ArrayList<PhiPair> dependances = new ArrayList<PhiPair>();
        float rm = 0, vm = 0, bm = 0;
        double delta = 0;

        for(int i = 0 ; i < 4 ; ++i) {

            if (childs[i].color != null) {

                temp[i] = childs[i].color ;

            } else {

                PhiPair childPair = childs[i].compressPhi(pairs);
                temp[i] = childPair.compressedColor;
                dependances.add(childPair);

            }

            rm += temp[i].getRed();
            vm += temp[i].getGreen();
            bm += temp[i].getBlue();

        }

        rm = rm/4;
        vm = vm/4;
        bm = bm/4;

        for(int i = 0 ; i < 4 ; ++i) {

            delta = Math.max(Math.sqrt((Math.pow(temp[i].getRed() - rm, 2) + Math.pow(temp[i].getGreen() - rm, 2) + Math.pow(temp[i].getBlue() - rm, 2)) / 3), delta);

        }

        int index = 0 ;

        while(dependances.size() >0){

            for(int i = 0 ; i < dependances.size() ; ++i){
                if(pairs.get(index).equals(dependances.get(i))){
                    dependances.remove(i);
                }
            }
            index++ ;

        }

        while(index < pairs.size() && pairs.get(index).delta < delta){
            index ++ ;
        }

        newPair = new PhiPair(delta,this, new Color((int)rm,(int)vm,(int)bm));
        pairs.add(index,newPair);
        return newPair ;

    }

    private void phiCompressThis(Color localColor){

        color = localColor ;
        for(int i = 0 ; i < 4 ; ++i){
            childs[i] =  null ;
        }

    }

    public String toString(){

        if(childs[0] == null){
            return img.colorToHex(color);
        } else {
            return "( " + childs[0].toString() + " " + childs[1].toString() + " " + childs[2].toString() + " " + childs[3].toString() + " )";
        }
    }

    public int getSize(){

        int size = 0 ;
        if(childs[0] == null){
            size ++ ;
        } else {
            for(int i = 0 ; i < 4 ; ++i){
                size+= childs[i].getSize();
            }
        }
        return size ;

    }

    //library
    public static int log(int x, int b){
        return (int) (Math.log(x) / Math.log(b));
    }


}
