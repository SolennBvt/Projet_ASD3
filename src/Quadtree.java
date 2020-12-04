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

        public double delta ; //delta maximum de noeud, calculé seulement si tous les fils sont des feuilles
        public Quadtree target; //Quadtree cible
        public PhiPair father ; //pere de la target, et non pere dans l'abr, ce noeud n'est pas encore dans l'arbre.
        public Color compressedColor; //couleur compréssé a remplacé si le noeud venait a etre compressé, calculé seulement si tous les fils sont des feuilles

        public PhiPair leftChild = null; //pointeurs vers les fils
        public PhiPair rightChild = null;

        public PhiPair(Quadtree target,PhiPair father){
            this.target = target ;
            this.father = father ;
        }

        /*
        * mise a jour du noeud a faire juste avant de l'intégrer dans l'arbre, permet d'obtenir son delta afin de le trier.
         */
        public void updateLeaf(double delta, Color compressedColor){
            this.delta = delta ;
            this.compressedColor = compressedColor ;
        }

        /*
        * permet de savoir si un noeud contient 4 fils feuilles, ce qui permetterait de le compresser.
         */
        public boolean isLeaf(){
            if(target == null){
                return false ;
            }
            for(int i = 0 ; i < 4 ; ++i){
                if(target.childs[i].color == null){
                    return false ;
                }
            }
            return true ;
        }

        /*
        * insertion d'un noeud dans l'arbre
        * le trie s'éffectue sur la valeur de delta
         */
        public void insert(PhiPair localTarget){

            if(target == null){

                if(leftChild == null){
                    leftChild = localTarget ;
                }else {
                    leftChild.insert(localTarget);
                }

            }else if(localTarget.delta <= this.delta){

                if(leftChild == null ){
                    leftChild = localTarget ;
                }else {
                    leftChild.insert(localTarget);
                }

            }else {

                if(rightChild == null){
                    rightChild = localTarget ;
                }else {
                    rightChild.insert(localTarget);
                }

            }

        }

        /*
        * sort le noeud de l'arbre le plus a gauche, et le supprime de l'arbre
         */
        public PhiPair pop(){
            if(target == null && leftChild == null){

                return null ;

            }else if(leftChild.leftChild == null) {

                PhiPair returnObj = leftChild ;
                leftChild = leftChild.rightChild ;
                return  returnObj ;

            }else {

                return leftChild.pop();

            }

        }

        @Override
        public String toString() {
            if(target == null){
                return leftChild.toString();
            }

            String returnString = " < "+delta;
            if(leftChild != null){
                returnString = leftChild.toString() + returnString ;
            }if(rightChild != null) {
                returnString = returnString + rightChild.toString();
            }
            return  returnString ;

        }
    }

    /*
    * fonction public compressPhi
    * initialise une Phipair racine
    * lit le quadtree en récursive grace a la fonction compressPhiReq
    * crée un ABR Phipair a partir de la de la racine qui contient tous les Quadtree pouvant etre compressé extrait de la lecture de l'arbre Quadtree
    * le compresse jusqu'a que le nombre de feuilles (leafs) soit plus faible que l'entier phi donné en argument
     */
    public void compressPhi(int phi) {

        int leafs = 0 ;
        PhiPair racine = new PhiPair(null,null);
        leafs += compressPhiReq(racine, null);

        PhiPair temp = racine.pop(); ;
        while (temp != null && leafs > phi){

            temp.target.phiCompressThis(temp.compressedColor);
            leafs -= 3; //a chaque compression, on détruit 4 feuilles pour en produire une, ce qui fait un changement net de 3.

            if(temp.father != null && temp.father.isLeaf()){

                temp.father.target.addPhiPair(racine, temp.father);

            }

            temp = racine.pop();

        }

    }

    /*
    * fonction récursive, lit l'arbre Quadtree
    * crée un phiPair pour chaque noeud de Quadtree,
    * les noeud possédant feuilles sont ajouté dans l'ABR de racine donné en argument,
    * retourne le nombre de feuilles renconrtré.
     */
    private int compressPhiReq(PhiPair racine, PhiPair father){

        PhiPair newPair = new PhiPair(this,father) ;
        int childNumber = 0;
        int leafs = 0;

        for(int i = 0 ; i < 4 ; ++i) {

            if (childs[i].color == null) {

                leafs += childs[i].compressPhiReq(racine, newPair);
                childNumber++;

            }

        }

        if(childNumber > 0){

            return leafs + (4-childNumber) ; //le nombre de feuilles correspond au nombre de fils qui ont une couleur non null

        }else {

            addPhiPair(racine,newPair);
            return leafs + 4 ;

        }

    }

    /*
    * calcule delta pour un noeud qui possede feuilles,
    * le PhiPair corespondant sera rajouté dans l'ABR.
     */
    private void addPhiPair(PhiPair racine, PhiPair newPair){

        double rm = 0, vm = 0, bm = 0;
        double delta = 0;

        for(int i = 0 ; i < 4 ; ++i){

            rm += childs[i].color.getRed();
            vm += childs[i].color.getGreen();
            bm += childs[i].color.getBlue();

        }

        rm = rm/4;
        vm = vm/4;
        bm = bm/4;

        for(int i = 0 ; i < 4 ; ++i) {

            delta = Math.max(Math.sqrt((Math.pow(childs[i].color.getRed() - rm, 2) + Math.pow(childs[i].color.getGreen() - rm, 2) + Math.pow(childs[i].color.getBlue() - bm, 2)) / 3), delta);

        }

        newPair.updateLeaf(delta,new Color((int)rm,(int)vm,(int)bm));

        racine.insert(newPair);

    }

    /*
    * compresse un noeud, a partir des information fournit par le Phipair correspondant.
     */
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