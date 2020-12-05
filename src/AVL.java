
public class AVL<Type extends Comparable> { // Classe T générique

    Type element; // si null, l'avl est vide
    int bal; // balance de l'arbre égal à 0, 1 ou -1
    AVL<Type> left; // fils gauche
    AVL<Type> right; // fils droit

    // Constructeurs
    public AVL(){
        element = null;
        bal = 0;
        left = null;
        right = null;
    }
    private AVL(Type value, AVL<Type> left, AVL<Type> right) {
        this.element = value;
        this.left = left;
        this.right = right;
    }

    // Méthodes

    public int insert(Type elt) {

        int h = 0;

        if (element == null) {

            element = elt;
            bal = 0;
            return 1;

        } else if (elt.compareTo(element) > 0) { // elt > element donc insertion à droite

            if (right == null) { right = new AVL<Type>(); }
            h = right.insert(elt);

        } else { // elt <= element donc insertion à gauche

            if(elt != element) {

                if (left == null) { left = new AVL<Type>(); }
                h = -left.insert(elt);
            }
        }

        // Balance et Equilibre
        if(h == 0){ return 0; }
        else {
            bal = bal + h;
            balance();
            if(bal == 0){ return 0; } else { return 1; }
        }


    }
    /*
    public int suppressMin(AVL<Type> father) {

        int h;

        if (left == null) { // on est au minimum

            if(father == null){ // on est à la racine

                if(right != null){
                    this.element = right.element;
                    this.bal = right.bal;
                    this.left = right.left;
                    this.right = right.right;
                } else {
                    this.element = null;
                }
            }else if (right !=null){

                father.left = right;

            } else {
                father.left = null;
            }
            return -1;

        } else { // on continue de chercher à gauche

            h = -left.suppressMin(this);
            if (h == 0) { return 0; }
            else {
                bal = bal + h;
                balance();
                if(bal == 0){return -1;} else {return 0;}
            }
        }
    }

     */

    public void remove(Type elt){
        remove(elt, this);
    }
    private int remove(Type elt, AVL<Type> father){

        int h;

        if(this.element == null){
            return 0;

        } else if (elt.compareTo(element) > 0) { // on va à droite
            h = right.remove(elt, this);

        } else if (elt.compareTo(element) < 0) { // on va à gauche
            h = -left.remove(elt, this);

        }
        //on est au noeud à supprimer
        else {

            AVL<Type> temp;

            // 0 ou 1 fils
            if( (left == null) || (right == null) ) {

                if (left != null) { // A <--- A.g

                    this.element = left.element;
                    this.right = left.right;
                    this.left = left.left;

                } else if (right != null){ // A <--- A.d

                    this.element = right.element;
                    this.left = right.left;
                    this.right = right.right;

                } else { // pas d'enfants, le noeud est supprimé

                    if(father == this){

                        this.element = null;

                    } else {

                        if(father.left == this){

                            father.left = null;

                        } else {

                            father.right = null;
                        }
                    }

                }
                return -1;

            } else { // 2 fils, A <--- Min(A.d) puis suppressMin(A.d)

                AVL<Type> rightMin = this.right.getMin();
                this.element = rightMin.element;

                h = this.right.remove(rightMin.element, this);
            }

        }
        // Balance et Equilibre
        if (h == 0) { return 0;}
        else {
            bal = bal + h;
            balance();
            if(bal == 0) { return -1; } else { return 0; }
        }
    }

    /*
     * Retourne le noeud contenant le minimum de l'AVL
     */
    public AVL<Type> getMin(){
        if(left == null){
            return this;
        } else {
            return left.getMin();
        }
    }
    public int numberOfNodes(){
        if(left == null || right == null){
            if(left != null){
                return 1 + left.numberOfNodes();
            } else if(right != null) {
                return 1 + right.numberOfNodes();
            } else {
                return 1;
            }
        } else {
            return 1 + left.numberOfNodes() + right.numberOfNodes();
        }
    }
    private void balance(){
        if(bal == 2){
            if(right.bal >= 0){
                rotG();
            } else {
                right.rotD();
                rotG();
            }
        } else if (bal == -2){
            if(left.bal <= 0){
                rotD();
            } else {
                left.rotG();
                rotD();
            }
        }
    }
    private void rotG(){

        AVL<Type> temp = new AVL();
        AVL<Type> B;
        int a, b;

        temp.element = this.element;
        temp.left = this.left;
        temp.right = this.right;
        temp.bal = this.bal;

        B = temp.right;

        //initialisation des balances
        a = this.bal;
        b = B.bal;

        // rotation
        temp.right = B.left;
        B.left = temp;

        //calcul des nouvelles balances
        temp.bal = a - Math.max(b, 0) - 1;
        B.bal = Math.min(Math.min(a-2, a+b-2), b-1);

        //this = B
        this.bal = B.bal;
        this.element = (Type) B.element;
        this.right = B.right;
        this.left = B.left;
    }
    private void rotD(){

        AVL<Type> temp = new AVL();
        AVL<Type> B;
        int a, b;

        temp.element = this.element;
        temp.left = this.left;
        temp.right = this.right;
        temp.bal = this.bal;

        B = this.left;

        //initialisation des balances
        a = this.bal;
        b = B.bal;

        // rotation
        temp.left = B.right;
        B.right = temp;

        //calcul des nouvelles balances
        temp.bal = a - Math.min(b, 0) + 1;
        B.bal = Math.max(Math.max(a+2, a+b+2), b+1);

        //this = B
        this.bal = B.bal;
        this.element = (Type) B.element;
        this.right = B.right;
        this.left = B.left;
    }

    public String toString(){

        if(left == null && right == null){
            return element.toString();
        } else if (left != null && right != null){
            return element.toString()+ "( " + left.toString() + " " + right.toString() + " )";
        } else if (right == null){
            return element.toString() + "( "  + left.toString() + " )";
        } else {
            return element.toString()+ "( " + right.toString() + " )";
        }
    }
}