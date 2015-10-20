import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.omg.CORBA.ARG_IN;
import sun.org.mozilla.javascript.ast.WhileLoop;

import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

/**
 * Created by min on 10/5/15.
 */
public class DPLLSearch {
    public class Atom {
        int atomIndex;
        String atomValue;
    }
    public static ArrayList<ArrayList<String>> clauseSet = new ArrayList<>();
    public static ArrayList<Integer> atoms = new ArrayList<>();
    public static ArrayList<Integer> pureLiteral = new ArrayList<>();
    public static DPLLSearch dp = new DPLLSearch();
    public static Atom pickAtom = dp.new Atom();
    public static Stack<Atom> exploredAtom = new Stack<>();
    public static ArrayList<Atom> allAtoms = new ArrayList<>();
    public static boolean restInfo = false;
    public static ArrayList<String> restInformation = new ArrayList<>();

    public static void addAtoms (ArrayList<String> arrayList) {
        for (String temp:arrayList) {
            if (!allAtoms.contains(Math.abs(Integer.parseInt(temp)))) {
                Atom tempAtom = dp.new Atom();
                tempAtom.atomIndex = Math.abs(Integer.parseInt(temp));
                allAtoms.add(tempAtom);
            }
        }
    }

    public static void readClauseSet (String inputFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile));
            String line = null;
            while((line = bufferedReader.readLine()) != null) {
                if (!restInfo) {
                    if (line.compareTo("0") != 0) {
                        ArrayList<String> tempSplit = new ArrayList<>(Arrays.asList(line.split("\\s+")));
                        clauseSet.add(tempSplit);
                        addAtoms(tempSplit);
                    } else {
                        restInfo = true;
                        restInformation.add(line);
                    }
                } else {
                    restInformation.add(line);
                }
            }

            bufferedReader.close();
            System.out.println(restInformation);
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            inputFile + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + inputFile + "'");
        }
    }

    public static boolean unsatisfiedSet(ArrayList<ArrayList<String>> newClauseSet) {
        for (int i = 0; i < newClauseSet.size(); i++) {
            if ((newClauseSet.get(i).size() == 1) && (newClauseSet.get(i).get(0).equals("false"))) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<ArrayList<String>> updateClauseSet (Atom atom, ArrayList<ArrayList<String>> newClauseSet){
        ArrayList<ArrayList<String>> copyclauseSet = new ArrayList<>();
        if (atom.atomValue.equals("true")) {
            for (int i = 0; i < newClauseSet.size(); i++) {
                ArrayList<String> temp = new ArrayList<String>(newClauseSet.get(i));
                if(temp.contains(String.valueOf(atom.atomIndex))) {
                    temp.clear();
                }
                if((temp.contains(String.valueOf(-(atom.atomIndex)))) && (temp.size() == 1)) {
                    temp.clear();
                    temp.add("false");
                }
                if((temp.contains(String.valueOf(-(atom.atomIndex)))) && (temp.size() > 1)) {
                    temp.remove(String.valueOf(-(atom.atomIndex)));
                }
                if (!temp.isEmpty()) {
                    copyclauseSet.add(temp);
                }
            }
        }

        if (atom.atomValue.equals("false")) {
            for (int i = 0; i < newClauseSet.size(); i++) {
                ArrayList<String> temp = newClauseSet.get(i);
                if(temp.contains(String.valueOf(-(atom.atomIndex)))) {
                    temp.clear();
                }
                if((temp.contains(String.valueOf(atom.atomIndex))) && (temp.size() > 1)) {
                    temp.remove(String.valueOf(atom.atomIndex));
                }
                if((temp.contains(String.valueOf(atom.atomIndex))) && (temp.size() == 1)) {
                    temp.clear();
                    temp.add("false");
                }
                if (!temp.isEmpty()) {
                    copyclauseSet.add(temp);
                }
            }
        }
        return copyclauseSet;
    }

    public static boolean singleton (ArrayList<ArrayList<String>> newClauseSet) {
        for (ArrayList<String> temp : newClauseSet) {
            if (temp.size() == 1) {
                pickAtom.atomIndex = Integer.parseInt(temp.get(0));
                pickAtom.atomValue = "true";
                return true;
            }
        }
        return false;
    }

    public static boolean pureLiteral (ArrayList<ArrayList<String>> newClauseSet) {
        for (int i = 0; i < newClauseSet.size(); i++) {
            for (int j = 0; j < newClauseSet.get(i).size(); j++) {
                int temp = Integer.parseInt(newClauseSet.get(i).get(j));
                if (!atoms.contains(Math.abs(temp))) {
                    if (pureLiteral.contains(-temp)) {
                        atoms.add(Math.abs(temp));
                        pureLiteral.remove(new Integer(-temp));
                    } else if (!pureLiteral.contains(temp)) {
                        pureLiteral.add(temp);
                    }
                }
            }
        }
        Collections.sort(pureLiteral);
        if (!pureLiteral.isEmpty()) {
            pickAtom.atomIndex = pureLiteral.get(0);
            pickAtom.atomValue = "true";
            pureLiteral.remove(0);
            return true;
        } else {
            return false;
        }
    }

    public static boolean chooseAtom (ArrayList<ArrayList<String>> newClauseSet) {
        int tempMin = Math.abs(Integer.parseInt(newClauseSet.get(0).get(0)));
        for (int i = 0; i < newClauseSet.size(); i++) {
            for (int j = 0; j < newClauseSet.get(i).size(); j++) {
                if (tempMin > Math.abs(Integer.parseInt(newClauseSet.get(i).get(j)))) {
                    tempMin = Math.abs(Integer.parseInt(newClauseSet.get(i).get(j)));
                }
            }
        }
        pickAtom.atomIndex = tempMin;
        pickAtom.atomValue = "true";
        return true;



    }

    public static ArrayList<ArrayList<String>> falseUpdateClause (ArrayList<ArrayList<String>> newClauseSet) {
        ArrayList<ArrayList<String>> temp = new ArrayList<>(newClauseSet);
        for (int i = 0; i < exploredAtom.size(); i++) {
            temp = updateClauseSet(exploredAtom.get(i), temp);
        }
        return temp;
    }


    public static boolean dpll (ArrayList<ArrayList<String>> newClauseSet) {
        ArrayList<ArrayList<String>> tempClause = new ArrayList<>();
        Atom tempPick = dp.new Atom();
        if (newClauseSet.isEmpty()) {
            return true;
        } else if (unsatisfiedSet(newClauseSet))  {
            Atom tempAtom = dp.new Atom();
            tempAtom = exploredAtom.pop();
            while (tempAtom.atomValue.equals("false")) {
                tempAtom = exploredAtom.pop();
            }
            tempAtom.atomValue = "false";
            exploredAtom.push(tempAtom);
            tempClause = falseUpdateClause(clauseSet);
            if  (dpll(tempClause)) {
                return true;
            }
        } else {
            if (singleton(newClauseSet)) {
                tempPick.atomValue = pickAtom.atomValue;
                tempPick.atomIndex = pickAtom.atomIndex;
                exploredAtom.add(tempPick);
                tempClause = updateClauseSet(pickAtom, newClauseSet);
                if (dpll(tempClause)) {
                    return true;
                }
            } else if (pureLiteral(newClauseSet)) {
                tempPick.atomValue = pickAtom.atomValue;
                tempPick.atomIndex = pickAtom.atomIndex;
                exploredAtom.add(tempPick);
                tempClause = updateClauseSet(pickAtom, newClauseSet);
                if (dpll(tempClause)) {
                    return true;
                }
            } else if (chooseAtom(newClauseSet)){
                tempPick.atomValue = pickAtom.atomValue;
                tempPick.atomIndex = pickAtom.atomIndex;
                exploredAtom.add(tempPick);
                tempClause = updateClauseSet(pickAtom, newClauseSet);
                if (dpll(tempClause)) {
                    return true;
                }
            }

        }
        System.out.println("No Solution.");
        return false;
    }

    /*public static void printOutput () {
        Collections.sort(allAtoms);
        for (Integer temp:allAtoms) {
            for (Atom tempExplore:exploredAtom) {
                if (temp == tempExplore.atomIndex) {
                    System.out.println(tempExplore.atomIndex + " " + tempExplore.atomValue);
                    /*allAtoms.remove(temp);*/
                /*}
                if (temp == (-tempExplore.atomIndex)) {
                    if (tempExplore.atomValue.equals("true")) {
                        System.out.println(temp + " false");
                    }
                    if (tempExplore.atomValue.equals("false")) {
                        System.out.println(temp + " true");
                    }
                    /*allAtoms.remove(temp);*/
                /*}
            }
        }

        /*if (!allAtoms.isEmpty()) {
            for (Integer temp:allAtoms) {
                System.out.println(temp + " true");
            }
        }*/


    public  static  void main (String[] args) {
        String inputFile = null;
        if (args.length > 0) {
            inputFile = args[0];
        }
        readClauseSet(inputFile);
        dpll(clauseSet);
        /*printOutput();*/
    }
}