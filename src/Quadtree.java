import java.awt.*;
import java.util.*;

public class Quadtree {

    private Quadtree childs[];
    private Color color ;
    private ImagePNG img;
    private Quadtree father;

    /*
     * Construit un quadtree compressé sans dégradation
     */
    public Quadtree(ImagePNG img, int x, int y, int length, Quadtree father){

        this.img = img;
        this.childs = new Quadtree[4];
        this.father = father;

        //si tous les pixels de la zone concernée sont de la même couleur alors on crée une feuille
        if(isSameColor(img, x, y, length)){

            this.color = img.getPixel(x, y);

        } else { //sinon création d'un noeud -> on divise la zone en 4 sous-région N0, NE, SE, SO

            int pas = length /2; //taille du sous-carré

            //matrice servant à calculer les nouvelles coordonnées (x, y) des sous-régions
            byte matrice[][] = {{0,0},{1,0},{1,1},{0,1}};

            for(int i = 0; i < 4; i++){

                //création des quatre fils correspondant aux sous-régions
                this.childs[i] = new Quadtree(img, x + matrice[i][0] * pas, y + matrice[i][1] * pas, pas, this);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
            if(allSonsAreLeaves()){ //condition 1 : tous les fils sont des feuilles

                double lambda = maxColorimetricDifference();

                if(lambda <= delta){ //condition 2 : écart_colorimétrique_max <= delta |---> compression
                    transformToLeaf(mediumColor());
                }
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void compressPhi(int phi){

        AVL<PhiStruct> colorDiffAVL = new AVL(); // AVL qui contiendra les noeud pères de 4 feuilles
        //TreeSet<PhiStruct> colorDiffAVL = new TreeSet<PhiStruct>();
        int nbLeaves = numberOfLeaves(); // nombre de feuilles du quadtree

        this.compressPhi_rec(colorDiffAVL);
        PhiStruct minColorDiff = colorDiffAVL.getMin().element; // le noeud minimal de l'AVL

        while(nbLeaves > phi && minColorDiff != null) {

            colorDiffAVL.remove(minColorDiff);
            minColorDiff.target.transformToLeaf(minColorDiff.compressedColor);

            nbLeaves -=3;

            if(minColorDiff.target.father != null && minColorDiff.target.father.allSonsAreLeaves()){

                PhiStruct father = new PhiStruct(minColorDiff.target.father.maxColorimetricDifference(),minColorDiff.target.father,minColorDiff.target.father.mediumColor());
                colorDiffAVL.insert(father);
            }

            minColorDiff = colorDiffAVL.getMin().element;
        }
    }

    private class PhiStruct implements Comparable{
        public double lambda ; //delta maximum de noeud, calculé seulement si tous les fils sont des feuilles
        public Quadtree target; //Quadtree cible
        public Color compressedColor; //couleur compréssé a remplacé si le noeud venait a etre compressé, calculé seulement si tous les fils sont des feuilles

        private PhiStruct(double lambda, Quadtree target, Color compressedColor){
            this.lambda = lambda ;
            this.target = target ;
            this.compressedColor = compressedColor ;
        }

        public int compareTo(Object o) {

            if(o instanceof PhiStruct){

                PhiStruct q = (PhiStruct)o;

                if(this.lambda > q.lambda){

                    return 1;

                } else if(this.lambda < q.lambda){

                    return -1;

                } else {

                    if(this.equals(q)) {
                        return 0;
                    }
                }
            }
            return -1;
        }

    }

    /*
     * Remplis un AVL avec les noeuds du quadtree qui n'ont que des feuilles comme fils
     */
    private void compressPhi_rec(AVL<PhiStruct> colorDiffAVL){

        if(this.color == null) { // on est à un noeud

            if (allSonsAreLeaves()) { // tous nos fils sont des feuilles

                PhiStruct newObjet = new PhiStruct(this.maxColorimetricDifference(),this,mediumColor());
                colorDiffAVL.insert(newObjet); // on insert l'écart colorimétrique max dans l'AVL

            } else {

                for (int i = 0; i < 4; i++) {
                    childs[i].compressPhi_rec(colorDiffAVL);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * Retourne la couleur moyenne des feuilles d'un noeud.
     * Pré-condition : le quadtree appelant doit être un noeud et tous ses fils des feuilles
     */
    private Color mediumColor(){

        double rm = 0, gm = 0, bm = 0;

        for(int i = 0 ; i < 4 ; i++){
            rm += childs[i].color.getRed();
            gm += childs[i].color.getGreen();
            bm += childs[i].color.getBlue();
        }
        rm = rm/4;
        gm = gm/4;
        bm = bm/4;

        return new Color((int)rm,(int)gm,(int)bm);
    }

    /*
     * Transforme un noeud en feuille. Sa couleur devient alors la couleur moyennes de ses fils.
     * Pré-condition : le quadtree appelant est un noeud et tous ses fils des feuilles.
     */
    private void transformToLeaf(Color tempColor){

        color = tempColor; // transformation en feuille

        for(int i = 0 ; i < 4 ; ++i){
            childs[i] = null ;
        }
    }

    /*
     * Retourne l'écart colorimétrique maximum de ses feuilles.
     * Pré-condition : le quadtree appelant est un noeud et tous ses fils des feuilles.
     */
    private double maxColorimetricDifference(){

        double lambda=0;

        //calcul de la couleur moyenne au noeud

        Color medCol = mediumColor();

        // calcul de l'écart colorimétrique max des feuilles du noeud.
        // Lambda = max( la_1, la_2, la_3, la_4) avec la_i = rac( 1/3 * ( (ri - rm)² + (gi - gm)² + (bi - bm)² ))

        for(int i = 0; i < 4; i++){

            lambda = Math.max(lambda, Math.sqrt((Math.pow(childs[i].color.getRed() - medCol.getRed(), 2) + Math.pow(childs[i].color.getGreen() - medCol.getGreen(), 2) + Math.pow(childs[i].color.getBlue() - medCol.getBlue(), 2)) /3));
        }

        return lambda;
    }

    /*
     * Retourne vrai si tous les fils sont des feuilles, faux sinon
     * Pré-condition : le quadtree appelant est un noeud et tous ses fils des feuilles
     */
    private boolean allSonsAreLeaves(){
        return childs[0].color != null && childs[1].color != null && childs[2].color != null && childs[3].color != null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * Retourne le nombre de feuille
     */
    public int numberOfLeaves(){

        if(childs[0] == null){ // on est au niveau d'une feuille car un noeud a forcément 4 fils

            return 1;

        } else {

            return childs[0].numberOfLeaves() + childs[1].numberOfLeaves() + childs[2].numberOfLeaves() + childs[3].numberOfLeaves();
        }
    }

    /*
     * Retourne une ImagePNG correspondant au Quadtree
     */
    public ImagePNG toPNG(){

        ImagePNG png = new ImagePNG(this.img);
        modifPNG_rec(png, 0, 0, this.img.height());

        return png;
    }

    /*
     * Modifie une image pour qu'elle corresponde au quadtree
     * @param x, y : entiers représentants la coordonnée (x, y) du point en haut à gauche
     * @param squareSize : taille du côté du carré de l'image à modifier (en pixel)
     */
    private void modifPNG_rec(ImagePNG png, int x, int y, int squareSize){

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

                this.childs[i].modifPNG_rec(png, x + matrix[i][0] * newSquareSize, y + matrix[i][1] * newSquareSize, newSquareSize );
            }
        }
    }

    /*
     * Retourne une chaîne de caractère représentant le quadtree
     * Pré-condition : quadtree non nul
     */
    public String toString(){

        if(childs[0] == null){

            return ImagePNG.colorToHex(color);

        } else {

            return "( " + childs[0].toString() + " " + childs[1].toString() + " " + childs[2].toString() + " " + childs[3].toString() + " )";
        }
    }

    /*
     *  Pour la classe AVL<Quadtree> : comparaison suivant l'écart colorimétrique pour insérer le Quadtree dans l'AVL
     */

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}