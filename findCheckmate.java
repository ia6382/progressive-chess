import Rules.Chessboard;
import Rules.Move;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.IOException;

public class findCheckmate {
    static String PATHinput = "testData/33.txt";

    public static void main(String[] args) throws IOException {
        //zacni merjenje casa (wall-clock)
        long casZacetek = System.currentTimeMillis();
        int stevecPresikanih = 0;
        int stevecNeveljavnih = 0;

        //preberi zacetno stanje sahovnice iz datoteke
        //String startFen = readFile(PATHinput);
        String startFen = readFile(args[0]);
        //System.out.println("zacetno stanje FEN: " + startFen);

        //zgoscena tabela closedSet ze preiskanih vozlisc
        HashSet<String> closedSet = new HashSet<String>();
        // prioritetna vrsta openSet vozlisc na "fronti"
        PriorityQueue<Vozlisce> openSet = new PriorityQueue<>();

        //na zacetku vsebuje le zacetno vozslisce
        Vozlisce zacetek = new Vozlisce(startFen);
        int stPotez = zacetek.sahovnica.getMovesLeft();
        int[] kralj = zacetek.poisciKralja();
        zacetek.fBestFirstSearch(stPotez, kralj[0], kralj[1], null);

        openSet.add(zacetek);

        while (openSet.isEmpty() == false) {
            //premakni iz openSet v closedSet
            Vozlisce novo = openSet.poll();

            //ce je mat vrni pot
            if(novo.sahovnica.getGameStatus() == 2){
                //System.out.println("nasel sem sah. FEN: " + novo.sahovnica.getFEN());
                izpisiPot(novo);
                break;
            }

            //preveri ce je trenutno vozlisce ilegalna poteza ali pa smo ga ze preiskali
            if(!(novo.sahovnica.getGameStatus() == 4)){
                stevecNeveljavnih ++;
                continue;
            }
            if(closedSet.contains(novo.sahovnica.getFEN())){
                continue;
            }
            closedSet.add(novo.sahovnica.getFEN());
            stevecPresikanih ++;


            //dodaj vse otroke v openSet
            ArrayList<Move> moznePoteze = novo.sahovnica.getMoves();
            for(int i = 0; i < moznePoteze.size(); i ++){
                Move otrokPoteza = moznePoteze.get(i);
                Chessboard otrokSahovnica = novo.sahovnica.copy();
                otrokSahovnica.makeMove(otrokPoteza);
                Vozlisce otrok = new Vozlisce(otrokSahovnica, otrokPoteza.toString(), novo);

                //izracunaj ceno in ga dodaj v openSet
                otrok.fBestFirstSearch(stPotez, kralj[0], kralj[1], otrokPoteza);
                //otrok.fWithPath(stPotez, 0.5, kralj[0], kralj[1], otrokPoteza);
                openSet.add(otrok);
            }

            /*//dodaj otroke v openSet ce jih nismo ze preiskali
            ArrayList<Vozlisce> otroci = novo.vrniOtroke();
            for(int i = 0; i < otroci.size(); i ++){
                Vozlisce otrok = otroci.get(i);
                if(closedSet.contains(otrok.sahovnica.getFEN())){
                    continue;
                }

                //mogoce preisci openSet ce ze imamo tega in ga ne dodaj

                //izracunaj ceno in ga dodaj v openSet
                otrok.fBestFirstSearch(kralj[0], kralj[1]);
                openSet.add(otrok);
            }*/
        }

        //izpis casa
        long cas = System.currentTimeMillis() - casZacetek;
        //System.out.println("elapsed time: "+ cas*0.001+"s");
        //System.out.println("preiskanih je bilo "+ stevecPresikanih+" vozlisc");
        //System.out.println("neveljavnih je bilo "+ stevecNeveljavnih+" vozlisc");
    }

    public static void izpisiPot(Vozlisce konec){
        ArrayList<String> zaporedje = new ArrayList<String>();
        while (konec.prejsnoVozlisce != null){
            zaporedje.add(konec.ime);
            konec = konec.prejsnoVozlisce;
        }

        for(int i = zaporedje.size()-1; i > 0; i --){
            System.out.print(zaporedje.get(i)+";");
        }
        System.out.println(zaporedje.get(0));
    }

    public static String readFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        String fen;
        try (Scanner fileScanner = new Scanner(path)) {
            //read a whole file into one string
            fen = fileScanner.useDelimiter("\\A").next();
        }
        return fen;
    }

}


class Vozlisce implements Comparable<Vozlisce>  {
    Chessboard sahovnica; //trenutno stanje sahovnice
    Vozlisce prejsnoVozlisce;
    String ime;
    double f; //skupna cena
    double g; //cena poti od start do tega vozlisca
    int h; //hevristicna cena od tega vozlisca do konca

    //konstruktor za start vozlisce (uporablja FEN)
    Vozlisce(String fen){
        sahovnica = Chessboard.getChessboardFromFEN(fen);
        prejsnoVozlisce = null;
        ime = "start";
    }

    //konstruktor za naslednja vozlisca
    Vozlisce(Chessboard sahovnica, String ime, Vozlisce prejsnoVozlisce){
        this.sahovnica = sahovnica;
        this.ime = ime;
        this.prejsnoVozlisce = prejsnoVozlisce;
    }

    //Otroci so vsa mozna stanja sahovnic, ki so posledica vseh moznih potez iz trenutnega stanja
    public ArrayList<Vozlisce> vrniOtroke(){
        ArrayList<Vozlisce> otroci = new ArrayList<Vozlisce>();
        ArrayList<Move> moznePoteze = sahovnica.getMoves();

        for(int i = 0; i < moznePoteze.size(); i ++){
            Move otrokPoteza = moznePoteze.get(i);
            Chessboard otrokSahovnica = sahovnica.copy();

            otrokSahovnica.makeMove(otrokPoteza);
            if(otrokSahovnica.getGameStatus() == 4 || otrokSahovnica.getGameStatus() == 2){
                otroci.add(new Vozlisce(otrokSahovnica, otrokPoteza.toString(), this));
            }
        }

        return otroci;
    }

    //Best first iskanje ima g enako 0
    public void fBestFirstSearch(int stPotez, int kraljX, int kraljY, Move prejsnaPoteza){
        g = 0;

        //int hm = 0;
        //if(sahovnica.getMovesLeft() >  stPotez/2) {
           double hm = hManhattan(kraljX, kraljY);
        //}
        //else{
            h = hCoveringGetMoves(kraljX, kraljY);
        //}

        //ArrayList<Move> moznePoteze = sahovnica.getMoves();
        //int k = hPromotion(moznePoteze, prejsnaPoteza);

        int stPorabljenihPotez = stPotez - sahovnica.getMovesLeft();
        f = 0.1*hm + 0.9*h - 0.8*stPorabljenihPotez;
        //f = h - 0.8*stPorabljenihPotez;
        if(prejsnaPoteza != null && (prejsnaPoteza.getPromotion() == 5 || prejsnaPoteza.getPromotion() == -5)){
            f = f - 0.5;
        }

    }

    //upostevamo se dolzino poti g (stevilo porabljenih potez)
    public void fWithPath(int stPotez, double alfa, int kraljX, int kraljY, Move prejsnaPoteza){
        h = hCoveringGetMoves(kraljX, kraljY);
        //h = hManhattan(kraljX, kraljY);

        int stPorabljenihPotez = stPotez - sahovnica.getMovesLeft();
        g = stPorabljenihPotez * (alfa/stPotez);
        f = g + h;
    }

    public int hPromotion(ArrayList<Move> moznePoteze, Move prejsnaPoteza){
        int k = 0;
        int[][] grid = sahovnica.getBoard();

        //ce je bil v prejsni potezi prmotion v kraljico je to najbolje
        if(prejsnaPoteza != null && (prejsnaPoteza.getPromotion() == 5 || prejsnaPoteza.getPromotion() == -5)){
            k += 7;
        }else{
            //od vseh premikov kmetov izberi tistega, ki je najbližje robu glede na to kam se je premaknil
            int min = 6;
            for(int i = 0; i < moznePoteze.size(); i ++) {
                Move premik = moznePoteze.get(i);
                int[] koordinate = premik.getCoordinates();
                int tox = koordinate[2];
                int fromx = koordinate[0];
                int fromy = koordinate[1];

                if(grid[fromx][fromy] == -1 || grid[fromx][fromy] == -1){
                    if(min > tox){
                        min = tox;
                    }
                }

            }
            k += 6 - min;
        }

        k = k*(-1);
        return k;
    }

    public int hCoveringGetMoves(int kraljX, int kraljY){
        int h = 0;
        int[][] grid = sahovnica.getBoard();

        ArrayList<Move> moznePoteze = sahovnica.getMoves();

        for(int i = 0; i < moznePoteze.size(); i ++){
            Move premik = moznePoteze.get(i);
            int[]koordinate = premik.getCoordinates();
            int tox = koordinate[2];
            int toy = koordinate[3];

            /*//ce je kmet
            int fromx = koordinate[0];
            int fromy = koordinate[1];
            if(grid[fromx][fromy] == -1 || grid[fromx][fromy] == -1){
                continue;
            }*/

            if(tox >= kraljX-1 && tox <= kraljX+1 && toy >= kraljY-1 && toy <= kraljY+1){
                h += 1;
            }

        }


        h = h*(-1);
        return h;
    }

    public int hManhattan(int kraljX, int kraljY){
        int h = 0;
        int[][] grid = sahovnica.getBoard();

        //izracunaj manhattensko razdaljo vseh igralcevih figur razn kmetov
        for(int x = 0; x < 8; x ++){
            for(int y = 0; y < 8; y++){
                if(sahovnica.getColor() == Chessboard.WHITE && grid[x][y] > 0){ // 1 je bel kmet. Do 6 so ostale bele figure
                    h = h + Math.abs(x - kraljX)+Math.abs(y - kraljY);
                }
                else if (sahovnica.getColor() == Chessboard.BLACK && grid[x][y] < 0){ // -1 je crni kmet. Do -6 so ostale crne figure
                    h = h + Math.abs(x - kraljX)+Math.abs(y - kraljY);
                }
            }
        }

        return h;
    }


    public int[] poisciKralja(){
        int[][] grid = sahovnica.getBoard();

        int kraljX = 0;
        int kraljY = 0;
        for(int x = 0; x < 8; x ++){
            for(int y = 0; y < 8; y++){
                if(sahovnica.getColor() == Chessboard.WHITE && grid[x][y] == sahovnica.KING_B){
                    kraljX = x;
                    kraljY = y;
                }
                else if (sahovnica.getColor() == Chessboard.BLACK && grid[x][y] == sahovnica.KING){
                    kraljX = x;
                    kraljY = y;
                }
            }
        }

        int[] kraljPozicija  = {kraljX, kraljY};
        return kraljPozicija;
    }

    // Comaprator za prioritetno vrsto
    @Override
    public int compareTo(Vozlisce v) {
        if(this.f > v.f) {
            return 1;
        } else if (this.f < v.f) {
            return -1;
        } else {
            return 0;
        }
    }
}
