import java.awt.*;
import java.util.ArrayList;

public class Quadtree {

    //attributs
    private Quadtree childs[];
    private Color color ;
    private ImagePNG img;

    //constructeur
    public Quadtree(ImagePNG img, int x, int y, int length){
        this.img = img;
        this.childs = new Quadtree[4];
        if(isSameColor(img, x, y, length)){
            this.color = img.getPixel(x, y);
        } else {
            int pas = length /2;
            int matrice[][] = {{0,0},{1,0},{1,1},{0,1}}; // NO, NE, SE, SO
            for(int i = 0; i < 4; i++){
                this.childs[i] = new Quadtree(img, x + matrice[i][0] * pas, y + matrice[i][1] * pas, pas);
            }
        }
    }

    private boolean isSameColor(ImagePNG img, int x, int y, int length){

        Color previousColor = img.getPixel(x, y);
        Color actualColor;
        int col = x + 1;
        int line = y;

        while((col < x + length) && (line < y + length)){

            actualColor = img.getPixel(col, line);

            if(actualColor.getRGB() != previousColor.getRGB()){
                return false;
            } else {
                col++;
                if(col == x + length) {
                    col = x;
                    line++;
                }
            }
        }
        return true;
    }

    //retourne la hauteur/profondeur de l'arbre (compressé sans dégradation lors de la construction)
    private int height(){
    boolean bool = false;
    int h = 0;
    for(int i = 0; i < 4; i++){
        if(this.childs[i] != null){
            bool = true;
            h = Math.max(h, this.childs[i].height());
        }
    }
    if(bool){
        h++;
    }
    return h;
}
    public void compressDelta(int delta){
        int h = this.height();
        for(int i = 0; i < h; i++){
            compressDelta_rec(delta, h - i);
        }
    }

    private void compressDelta_rec(int delta, int cpt){

        if(this.childs[0] != null){ // on est à un noeud
            float rm = 0, gm = 0, bm = 0;
            int r, g, b;
            boolean isCompressable = true;

            if(this.childs[0].color != null && this.childs[1].color != null && this.childs[2].color != null && this.childs[3].color != null){ // tous les enfants sont des feuilles

                for(int i = 0 ; i < 4 ; i++){
                    rm += childs[i].color.getRed();
                    gm += childs[i].color.getGreen();
                    bm += childs[i].color.getBlue();
                }
                rm = rm/4;
                gm = gm/4;
                bm = bm/4;

                for(int i = 0; i < 4; i++){
                    r = childs[i].color.getRed();
                    g = childs[i].color.getGreen();
                    b = childs[i].color.getBlue();

                    if(Math.sqrt((Math.pow(r - rm, 2) + Math.pow(g - gm, 2) + Math.pow(b - bm, 2)) /3) > delta){
                        isCompressable = false;
                    }
                }
                if(isCompressable){
                    color = new Color((int)rm,(int)gm,(int)bm);
                    for (int i = 0; i < 4; i++) {
                        childs[i] = null;
                    }
                }
            } else { // tous les enfants ne sont pas des feuilles
                if(cpt > 1){
                    for(int i = 0; i < 4; i++){
                        if(childs[i].color == null){
                            childs[i].compressDelta_rec(delta, cpt-1);
                        }
                    }
                }
            }
        }
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

    public ImagePNG toPNG(){

        ImagePNG png = new ImagePNG(this.img);
        modifPNG(png, 0, 0, this.img.height());

        return png;
    }
    private void modifPNG(ImagePNG png, int x, int y, int squareSize){

        if (this.color != null) { // feuille ou noeud compressé

            if (squareSize == 1) { // carré de 1px -> feuille
                png.setPixel(x, y, this.color);

            } else { // noeud compressé

                int col = x; int line = y;
                int xMax = x + squareSize - 1;
                int yMax = y + squareSize - 1;

                while((col < xMax) || (line < yMax)){

                    png.setPixel(col, line, this.color);

                    if(col == xMax){ // on a fini la ligne, on passe à la suivante
                        col = x;
                        line++;

                    } else { // on continue la ligne
                        col++;
                    }
                }
                png.setPixel(col, line, this.color);

            }
        } else { // noeud
            /*
            Img divisée en 4 sous-carrés avec une nouvelle taille = ancienne /2, nouvelles coordonnées :
            NO -> x,y
            NE -> x + taille, y
            SE -> x + taille, y + taille
            SO -> x, y + taille
             */
            int matrix[][] = {{0,0},{1,0},{1,1},{0,1}}; // NO -> childs[0], NE -> childs[1], SE-> childs[2], SO-> childs[3]
            int newSquareSize = squareSize /2;

            for(int i = 0; i < 4; i++) {

                this.childs[i].modifPNG(png, x + matrix[i][0] * newSquareSize, y + matrix[i][1] * newSquareSize, newSquareSize );
            }
        }
    }

    public String toString(){

        if(childs[0] == null){
            return ImagePNG.colorToHex(color);
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
