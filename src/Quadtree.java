import java.awt.*;
import java.util.ArrayList;

public class Quadtree {

    //attributs

    private Quadtree childs[];
    private Color color ;
    private ImagePNG img;

    //constructeur

    /*
         * Appel constructeur : new Quadtre(img, 0, 0, img.length);
         */
    public Quadtree(ImagePNG img, int x, int y, int length){

        this.img = img;
        this.childs = new Quadtree[4];

        if(isSameColor(img, x, y, length)){ //si tous les pixels de la zone concernée sont de la même couleur alors on crée une feuille

            this.color = img.getPixel(x, y);

        } else { //sinon création d'un noeud -> on divise la zone en 4 sous-région N0, NE, SE, SO

            int pas = length /2; //taille du sous-carré
            int matrice[][] = {{0,0},{1,0},{1,1},{0,1}}; //matrice servant à calculer les nouvelles coordonnées (x, y) des sous-régions

            for(int i = 0; i < 4; i++){

                this.childs[i] = new Quadtree(img, x + matrice[i][0] * pas, y + matrice[i][1] * pas, pas); //création des quatre fils correspondant aux sous-régions
            }
        }
    }

    //méthodes

    /*
     * Retourne si les pixels sont tous de la même couleur dans une sous-partie carrée d'une image
     *
     * @param ImagePNG img : image à examiner
     * @param int x, y : coordonnée (x, y) en haut à gauche du carré
     * @param int length : taille du côté du carré
     * @return : booleen
     */
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

    /*
     * Effectue la compression delta avec dégradation du quadtree selon une dégradation maximale autorisée
     * @param int delta : dégradation maximale autorisée, compris entre 0 et 255 (ou 192? --> deux valeurs différentes dans le sujet)
     */
    public void compressDelta(int delta){

        if(this.color == null){ //on est à un noeud

            for(int i = 0; i < 4; i++){ //pour chaque fils

                if(this.childs[i].color == null){ //si le fils est un noeud

                    this.childs[i].compressDelta(delta); //on "descend"
                }
            }
            //compression éventuelle
            double rm = 0, gm = 0, bm = 0;
            double lambda=0;
            boolean isCompressable = true;

            //condition 1 : tous les fils sont des feuilles
            for(int i = 0; i < 4; i++){
                isCompressable &= childs[i].color != null;
            }

            if(isCompressable){
                //calcul des valeur r, g, b moyennes des feuilles du noeud
                for(int i = 0 ; i < 4 ; i++){
                    rm += childs[i].color.getRed();
                    gm += childs[i].color.getGreen();
                    bm += childs[i].color.getBlue();
                }
                rm = rm/4;
                gm = gm/4;
                bm = bm/4;

                //calcul de l'écart colorimétrique lambda max des feuilles du noeud
                for(int i = 0; i < 4; i++){
                    lambda = Math.max(lambda, Math.sqrt((Math.pow(childs[i].color.getRed() - rm, 2) + Math.pow(childs[i].color.getGreen() - gm, 2) + Math.pow(childs[i].color.getBlue() - bm, 2)) /3));
                }
                //condition 2 : écart_colorimétrique_max <= delta |---> compression
                if(lambda <= delta){
                    color = new Color((int)rm,(int)gm,(int)bm); //la couleur du noeud est initialisé à la couleur moyenne des feuilles
                    for (int i = 0; i < 4; i++) {
                        childs[i] = null; //le noeud devient une feuille, donc on met les fils à null
                    }
                }
            }
        }
    }

    //////////////////////////////////////////
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
    /////////////////////////////////////////////

    /*
     * Retourne une ImagePNG correspondant au Quadtree
     */
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

                    if(col == xMax){ // on a fini de parcourir la ligne, on passe à la suivante
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