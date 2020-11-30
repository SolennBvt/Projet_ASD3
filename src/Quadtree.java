import java.awt.*;
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

    public boolean compressPhi(int phi){
        return false;
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
            return img.colorToHex(color);
        } else {
            return "( " + childs[0].toString() + " " + childs[1].toString() + " " + childs[2].toString() + " " + childs[3].toString() + " )";
        }
    }

    //library
    public static int log(int x, int b){
        return (int) (Math.log(x) / Math.log(b));
    }

}
