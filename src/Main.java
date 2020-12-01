import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class Main {

    public static Scanner sc = new Scanner(System.in);
    public static ArrayList<Integer> aAfficher = new ArrayList<Integer>();
    public static int prevCompress;
    public static String fileName, initialFileName;
    public static ImagePNG png, png_delta, png_phi;
    public static Quadtree qdTree, qdTree_delta, qdTree_phi;

    public static String T[][] = {
            /* 0 */    {"Charger une image PNG dans un Quadtree", "Indiquez le chemin de l'image PNG :"},
            /* 1 */    {"Appliquer une compression Delta", "Indiquez la dégradation maximale delta, doit être comprise entre 0 et 255 :"},
            /* 2 */    {"Appliquer une compression Phi", "Indiquez le poids maximal phi, doit être strictement supérieure à 0 :"},
            /* 3 */    {"Sauvegarder le quadtree dans un fichier PNG", "Sauvegarde vers PNG..."},
            /* 4 */    {"Sauvegarder la représentation textuelle du quadtree dans un fichier TXT", "Sauvegarde vers TXT..."},
            /* 5 */    {"Donner les mesures comparative de deux fichiers images PNG", "Mesures comparatives :"},
            /* 6 */    {"Recommencer avec une nouvelle image", "Indiquez le chemin de l'image PNG :"},
            /* 7 */    {"Arrêter le programme", "Fin du programme"},
    };

    // fonctions de menu
    public static void afficherMenu(){
        for(int i = 0; i < aAfficher.size(); i++){
            int num = aAfficher.get(i).intValue();
            System.out.println(num + " - " + T[num][0]);
        }
    }
    public static void afficherDemande(int num){
        System.out.println(T[num][1]);
    }
    public static void actualiserMenu(int numero){
        aAfficher.clear();
        switch(numero){
            case 0: //charger PNG dans Quadtree
                aAfficher.add(1);
                aAfficher.add(2);
                aAfficher.add(4);
                aAfficher.add(6);
                aAfficher.add(7);
                break;
            case 1: //compression Delta
                aAfficher.add(1);
                aAfficher.add(3);
                aAfficher.add(4);
                aAfficher.add(6);
                aAfficher.add(7);
                break;
            case 2: //compression Phi
                aAfficher.add(2);
                aAfficher.add(3);
                aAfficher.add(4);
                aAfficher.add(6);
                aAfficher.add(7);
                break;
            case 3: //sauvegarder dans PNG
                aAfficher.add(1);
                aAfficher.add(2);
                aAfficher.add(4);
                aAfficher.add(5);
                aAfficher.add(6);
                aAfficher.add(7);
                break;
            case 4: //sauvegarder dans TXT
                if(prevCompress == 0){
                    aAfficher.add(1);
                    aAfficher.add(2);
                    aAfficher.add(6);
                    aAfficher.add(7);
                } else {
                    aAfficher.add(1);
                    aAfficher.add(2);
                    aAfficher.add(3);
                    aAfficher.add(6);
                    aAfficher.add(7);
                }
                break;
            case 5: //comparaison entre deux PNGs
                aAfficher.add(1);
                aAfficher.add(2);
                aAfficher.add(3);
                aAfficher.add(4);
                aAfficher.add(5);
                aAfficher.add(6);
                aAfficher.add(7);
                break;
        }
    }
    public static void execFonction(int numero, int val){
        switch(numero){
            case 0:
                fileName = lireReponseString();
                initialFileName = fileName ;
                createQuadtree(fileName);
                fileName = nomFichier(fileName);
                break;
            case 1:
                delta(val);
                break;
            case 2:
                phi(val);
                break;
            case 3:
                saveToPNG(val);
                break;
            case 4:
                saveToTXT(val);
                break;
            case 5:
                comparePNG(val);
                break;
            case 6:
                aAfficher.clear();
                aAfficher.add(0);
                prevCompress = 0;
                break;
        }
    }

    // fonctions à exécuter
    public static void createQuadtree(String path){

        try{
            png = new ImagePNG(path);
            qdTree = new Quadtree(png, 0, 0, png.height());
            //System.out.println("Quadtree obtenu : ");
            //System.out.println(qdTree.toString());
            //System.out.println();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    public static void delta(int valeur){
        qdTree_delta = new Quadtree(png, 0, 0, png.height());
        qdTree_delta.compressDelta(valeur);
        //System.out.println("Quadtree obtenu avec une compression Delta de " + valeur + " : ");
        //System.out.println(qdTree_delta.toString());
        //System.out.println();
    }
    public static void phi(int valeur){
        qdTree_phi = new Quadtree(png, 0, 0, png.height());
        qdTree_phi.compressPhi(valeur);
        //System.out.println("Quadtree obtenu avec une compression Phi de " + valeur + " : ");
        //System.out.println(qdTree_phi.toString());
        //System.out.println();
    }

    public static String nomFichier(String path){
        boolean findDeb = false;
        boolean findEnd = false;
        int cursor = path.length() - 1, deb = 0, end = 0;
        String toReturn = "";

        while((!findDeb || !findEnd) && cursor >= 0){
            if(path.charAt(cursor) == '.'){
                end = cursor;
                findEnd = true;
            }
            if(path.charAt(cursor) == '/'){
                deb = cursor;
                findDeb = true;
            }
            cursor--;
        }
        for(int i = deb +1; i < end; i++){
            toReturn += path.charAt(i);
        }
        return toReturn;
    }

    public static void saveToPNG(int val){

        String newFileName = "";

        try {

            if(prevCompress == 1){

                png_delta = qdTree_delta.toPNG();
                newFileName = "results/" + fileName + "-delta" + val +".png";
                png_delta.save(newFileName);

            } else if(prevCompress == 2){

                png_phi = qdTree_phi.toPNG();
                newFileName = "results/" + fileName + "-phi" + val +".png";
                png_phi.save(newFileName);
            }
            System.out.println("fichier " + "\"" +  newFileName + "\"" + " créé");
            System.out.println();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }
    public static void saveToTXT(int val){

        String newFileName = "";

        try {

            if(prevCompress == 0){

                newFileName = "results/" + fileName + ".txt";
                File fichier = new File(newFileName) ;
                PrintWriter out = new PrintWriter(new FileWriter(fichier)) ;
                out.write(qdTree.toString()) ; //écris dans le fichier
                out.close() ; //Ferme le flux du fichier, sauvegardant ainsi les données.

            } else if(prevCompress == 1){

                newFileName = "results/" + fileName + "-delta" + val + ".txt";
                File fichier = new File(newFileName) ;
                PrintWriter out = new PrintWriter(new FileWriter(fichier)) ;
                out.write(qdTree_delta.toString()) ; //écris dans le fichier
                out.close() ; //Ferme le flux du fichier, sauvegardant ainsi les données.

            } else if(prevCompress == 2){

                newFileName = "results/" + fileName + "-phi" + val +".txt";
                File fichier = new File(newFileName) ;
                PrintWriter out = new PrintWriter(new FileWriter(fichier)) ;
                out.write(qdTree_phi.toString()) ; //écris dans le fichier
                out.close() ; //Ferme le flux du fichier, sauvegardant ainsi les données.
            }
            System.out.println("fichier " + "\"" +  newFileName + "\"" + " créé");
            System.out.println();

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public static void comparePNG(int val){

        File fic = new File(initialFileName);

        if(prevCompress == 1) {
            double siD = ImagePNG.computeEQM(png,png_delta);
            File ficD =  new File("results/" + fileName + "-delta" + val + ".png");
            double wD = Math.ceil(10000.0*ficD.length() / fic.length())/100.0;
            System.out.println("delta : rapport de poids = "+wD+"% / qualité = "+siD+"%");
        }
        if(prevCompress == 2){
            double siP = ImagePNG.computeEQM(png,png_phi);
            File ficP =  new File("results/" + fileName + "-phi" + val + ".png");
            double wP = Math.ceil(10000.0*ficP.length() / fic.length())/100.0;
            System.out.println("phy : rapport de poids = "+wP+"% / qualité = "+siP+"%");
        }
    }

/*
    double siD = ImagePNG.computeEQM(png,png_delta);
    double siP = ImagePNG.computeEQM(png,png_phi);

    File fic = new File(args[0]);
    File ficD =  new File("results/" + fileName + "-delta" + delta + ".png");
    File ficP =  new File("results/" + fileName + "-phi" + phi + ".png");

    double wD = Math.ceil(10000.0*ficD.length() / fic.length())/100.0;
    double wP = Math.ceil(10000.0*ficP.length() / fic.length())/100.0;


    System.out.println("delta : rapport de poids = "+wD+"% / qualité = "+siD+"%");
    System.out.println("phy : rapport de poids = "+wP+"% / qualité = "+siP+"%");

 */

    // entrée clavier et vérif
    public static String lireReponseString(){
        System.out.print("--> ");
        return sc.nextLine();
    }
    public static int lireReponseInt(){
        System.out.print("--> ");
        int toReturn =  sc.nextInt();
        sc.nextLine();
        return toReturn;
    }
    public static boolean verifReponse(int numero){
        return aAfficher.contains(numero);
    }
    public static boolean verifValeur(int numero, int valeur){
        if(numero == 1){
            return valeur > 0 && valeur < 255;
        } else if (numero == 2) {
            return valeur > 0;
        } else {
            return true;
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main( String[] args ) {

        try {

            if(args.length == 0) {

                aAfficher.add(0); // 0 -> Charger une imagePNG dans un Quadtree

                System.out.println();
                System.out.println("Programme Projet ASD3 : Compression d'une image bitmap et Quadtree");
                System.out.println("CONSIGNES : Pour effectuer une action , tapez le numéro correspondant dans le menu.");
                System.out.println();

                int num = 0; // le numéro de l'action dans le menu
                int val = 0; // la valeur de phi ou delta

                boolean bool;

                while(true && num != 7){ // 7 -> Arrêter le programme

                    afficherMenu();

                    if(num != 0)
                    {
                        num = lireReponseInt();
                        System.out.println();
                    }

                    if(verifReponse(num)){ // l'entrée de l'utilisateur est correcte vis-à-vis du menu

                        afficherDemande(num);
                        bool = false;

                        if(num == 1 || num == 2) { // compression phi ou delta

                            while(!bool){

                                val = lireReponseInt();
                                prevCompress = num;

                                bool = verifValeur(num, val);

                                if(!bool){
                                    System.out.println("Erreur : vérifiez la saisie!");
                                }
                            }
                        }
                        execFonction(num, val);
                        actualiserMenu(num);
                        System.out.println();

                        if(num == 0)
                        {
                            num ++;
                        }

                    } else {

                        System.out.println("Erreur : vérifiez la saisie");
                    }
                }

            } else if (args.length == 3) {

                System.out.println();

                int delta = 0 , phi = 0 ;

                try{

                    createQuadtree(args[0]);
                    fileName = nomFichier(args[0]);

                }catch (Exception e){

                    System.out.println("le premier argument doit etre un chemin vers une image valide au format 2^n×2^n");

                }

                try{

                    delta = Integer.parseInt(args[1]);
                    phi = Integer.parseInt(args[2]);

                }catch (Exception e){

                    System.out.println("l'argument delta doit etre au format flottant er l'argument phi au format entier");

                }

                prevCompress = 1 ;
                delta(delta);
                saveToPNG(delta);
                saveToTXT(delta);

                prevCompress = 2 ;
                phi(phi);
                saveToPNG(phi);
                saveToTXT(phi);

                double siD = ImagePNG.computeEQM(png,png_delta);
                double siP = ImagePNG.computeEQM(png,png_phi);

                File fic = new File(args[0]);
                File ficD =  new File("results/" + fileName + "-delta" + delta + ".png");
                File ficP =  new File("results/" + fileName + "-phi" + phi + ".png");

                double wD = Math.ceil(10000.0*ficD.length() / fic.length())/100.0;
                double wP = Math.ceil(10000.0*ficP.length() / fic.length())/100.0;


                System.out.println("delta : rapport de poids = "+wD+"% / qualité = "+siD+"%");
                System.out.println("phy : rapport de poids = "+wP+"% / qualité = "+siP+"%");

            } else {

                throw new IllegalArgumentException("Préciser les 3 paramètres suivants: (1)chemin du fichier png (2)delta(entre 0 et 255) (3)phi(>0)");
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
