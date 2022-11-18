package curriculumdesign;



import org.junit.Test;

import javax.swing.*;
import java.util.*;

/**
 * @author 李天翔
 * @date 2022/06/13
 **/
@SuppressWarnings({"all"})
public class Priority {
    static char[][] table;
    // E->E+T|T#T->T*F|F#F->(E)|i i+i#
    //E->E+T|T#T->F*F|F#F->(E)|i  i+i*i#
    //V->N#V->N[E]#E->V#E->V+E#N->i [i+i+i]#
    static String[] grammar = {"V->N", "V->N[E]", "E->V", "E->V+E", "N->i"};
    static HashSet<Character> vtSet = new HashSet<>();//终结符
    static HashSet<Character> vnSet = new HashSet<>();//非终结符
    static HashMap<Character, ArrayList<String>> productionMap = new HashMap<>();//产生式Map
    static HashMap<Character, HashSet<Character>> firstVtMap = new HashMap<>();//任意非终结符的firstVt集
    static HashMap<Character, HashSet<Character>> lastVtMap = new HashMap<>();//任意非终极符的lastVt集
    static int row;
    static int col;
    static ArrayList<Step> steps = new ArrayList<>();
    static String inString;
    static Stack<Character> charStack = new Stack<>();
    static boolean right = true;
    static String errorMes="出错";
    /**
     * 用来得到终结符集和非终结符集
     */
    @Test
    public static void init() {
        steps.clear();
        vtSet.clear();
        vnSet.clear();
        firstVtMap.clear();
        lastVtMap.clear();
        productionMap.clear();
        charStack.clear();
        right=true;
        //生成终结符和非终结符集
        for (int i = 0; i < grammar.length; i++) {
            char[] temp = grammar[i].replace("->", "").replace("|", "").toCharArray();//去除->

            for (int j = 0; j < temp.length; j++) {
                if (temp[j] >= 'A' && temp[j] <= 'Z') {
                    vnSet.add(temp[j]);//大写字母为非终结符
                } else {
                    vtSet.add(temp[j]);
                }
            }
        }
        vtSet.add('#');

        //生成每个非终结符对应的产生式
        for (String str : grammar) {
            String[] strings = str.split("->")[1].split("\\|");
            char ch = str.charAt(0);
            ArrayList<String> list = productionMap.containsKey(ch) ? productionMap.get(ch) : new ArrayList<String>();
            for (String S : strings) {
                list.add(S);
            }
            productionMap.put(str.charAt(0), list);
            //System.out.println(str.charAt(0) + "\t" + list);
        }
        //System.out.println(vnSet + "***" + vnSet);
    }


    /**
     * 该函数用于得到两个终结符之间的优先级关系
     *
     * @param x1
     * @param x2
     * @return 返回值为优先关系表中的优先关系
     */
    public static char getPriority(char x1, char x2) {
        for (int i = 0; i < row; i++) {
            if (x1 == table[i][0]) {
                for (int j = 0; j < col; j++) {
                    if (x2 == table[0][j]) {
                        return table[i][j];
                    }
                }

            }
        }
        return ' ';
    }

    /**
     * 构建firstVt集
     */
    public static void getFirstVt() {
        HashSet<Character> hs = null;

        //用两个栈同步完成非终结符对应一个终结符，即在栈中相同位置处的非终结符的firstVt包含该终结符
        Stack<Character> stackVn = new Stack<>();
        Stack<Character> stackVt = new Stack<>();
        Set<Character> charVnSet = productionMap.keySet();
        for (Character charVn : charVnSet) {//进行一个初始化操作，遍历所有产生式，把产生式中所有P->a....或P->Qa.... 把a添加到P的firstVt中
            ArrayList<String> arrayList = productionMap.get(charVn);
            hs = firstVtMap.containsKey(charVn) ? firstVtMap.get(charVn) : new HashSet<Character>();//如果不存在则创建，存在则取出
            for (String s : arrayList) {
                if (vtSet.contains(s.charAt(0))) {//P->a...
                    hs.add(s.charAt(0));
                } else if (s.length() >= 2 && vtSet.contains(s.charAt(1))) {//P->Qa...
                    hs.add(s.charAt(1));
                }
            }
            for (Character h : hs) {//把P和它的firstVt集添加到栈中
                stackVn.push(charVn);
                stackVt.push(h);
            }
            firstVtMap.put(charVn, hs);
        }
        while (!stackVn.isEmpty()) {//如果栈STACK不空，就将顶项逐出，记此项为(Q，a).对于每个形如P->Q...的产生式,若P的fistvt集不包含a,则fistVt添加a并将(P,a)推进stack栈
            //取出栈顶
            Character peekVn = stackVn.peek();
            Character peekVt = stackVt.peek();
            stackVn.pop();
            stackVt.pop();

            for (Character charVn : charVnSet) {//遍历产生式
                ArrayList<String> arrayList = productionMap.get(charVn);
                hs = firstVtMap.containsKey(charVn) ? firstVtMap.get(charVn) : new HashSet<Character>();
                for (String s : arrayList) {
                    if (s.charAt(0) == peekVn) {//找到P->Q....
                        hs = firstVtMap.containsKey(charVn) ? firstVtMap.get(charVn) : new HashSet<Character>();
                        if (!hs.contains(peekVt)) {//如果不含a则入栈并添加
                            hs.add(peekVt);
                            stackVn.push(charVn);
                            stackVt.push(peekVt);
                            firstVtMap.put(charVn, hs);
                        }
                        break;
                    }
                }
            }
        }

//        Set<Character> characters = firstVtMap.keySet();
//        for (Character character : characters) {
//            System.out.println(firstVtMap.get(character));
//        }

    }

    /**
     * 构建lastVt集，算法和firstVt集类似，只不过是找P->.....a或P->.....aQ
     */
    public static void getLastVt() {
        //用两个栈同步完成非终结符对应一个终结符，即在栈中相同位置处的非终结符的lastVt包含该终结符
        HashSet<Character> hs = null;
        Set<Character> charVnSet = productionMap.keySet();
        Stack<Character> stackVn = new Stack<>();
        Stack<Character> stackVt = new Stack<>();

        for (Character charVn : charVnSet) {//进行一个初始化操作，遍历所有产生式，把产生式中所有P->....a或P->....aQ 把a添加到P的lastVt中
            ArrayList<String> arrayList = productionMap.get(charVn);
            hs = lastVtMap.containsKey(charVn) ? lastVtMap.get(charVn) : new HashSet<Character>();
            for (String s : arrayList) {
                if (vtSet.contains(s.charAt(s.length() - 1))) {
                    hs.add(s.charAt(s.length() - 1));
                } else if (s.length() >= 2 && vtSet.contains(s.charAt(s.length() - 2))) {
                    hs.add(s.charAt(s.length() - 2));
                }
            }
            for (Character h : hs) {//把P和它的lastVt集添加到栈中
                stackVn.push(charVn);
                stackVt.push(h);
            }
            lastVtMap.put(charVn, hs);
        }
        while (!stackVn.isEmpty()) {//如果栈STACK不空，就将顶项逐出，记此项为(Q，a).对于每个形如P->Q...的产生式,若P的fistvt集不包含a,则fistVt添加a并将(P,a)推进stack栈
            //取出栈顶
            Character peekVn = stackVn.peek();
            Character peekVt = stackVt.peek();
            stackVn.pop();
            stackVt.pop();

            for (Character charVn : charVnSet) {//遍历产生式
                ArrayList<String> arrayList = productionMap.get(charVn);
                hs = lastVtMap.containsKey(charVn) ? lastVtMap.get(charVn) : new HashSet<Character>();
                for (String s : arrayList) {
                    if (s.charAt(s.length() - 1) == peekVn) {//找到P->....Q
                        hs = lastVtMap.containsKey(charVn) ? lastVtMap.get(charVn) : new HashSet<Character>();
                        if (!hs.contains(peekVt)) {//如果不含a则入栈并添加
                            hs.add(peekVt);
                            stackVn.push(charVn);
                            stackVt.push(peekVt);
                            lastVtMap.put(charVn, hs);
                        }
                        break;
                    }
                }
            }
        }
//        Set<Character> characters = lastVtMap.keySet();
//        for (Character character : characters) {
//            System.out.println(lastVtMap.get(character));
//        }

    }

    /**
     * 根据firstvt和lastvt创建优先关系表
     */
    public static void creatTable() {
        row = vtSet.size() + 1;
        col = vtSet.size() + 1;
        //System.out.println(vtSet);
        table = new char[row][col];//创建二维字符数组
        //System.out.println(row+" "+col);
        int temp = 1;
        //初始化table表，为第一行和第一列赋值
        for (Character ch : vtSet) {
            table[0][temp] = ch;
            table[temp][0] = ch;
            temp++;
        }
        HashSet<Character> hashSet = firstVtMap.get('E');//添加#与终结符的优先关系 #E#
        for (Character character : hashSet) {
            addPri('#', character, '<');//# < E的firstVt集
        }
        hashSet = lastVtMap.get('E');
        for (Character character : hashSet) {
            addPri(character, '#', '>');//# > E的lastVt集
        }
        addPri('#', '#', '=');//# = #

        //通过非终结符，找到对应的产生式，遍历所有的产生式
        for (Character ch : vnSet) {
            ArrayList<String> arrayList = productionMap.get(ch);
            for (String s : arrayList) {
                char[] chars = s.toCharArray();//将候选式变成字符数组，遍历该数组
                for (int i = 0; i < chars.length - 1; i++) {
                    if (vtSet.contains(chars[i]) && vtSet.contains(chars[i + 1])) {//如果出现 ab的情况，即两终结符相邻 优先关系为 =
                        addPri(chars[i], chars[i + 1], '=');
                        if (right == false) {

                            return;
                        }
                    }
                    //如果出现 aBc 即终结符 非终结符 终结符 两终结符关系为 =
                    if (i < chars.length - 2 && vtSet.contains(chars[i]) && vtSet.contains(chars[i + 2]) && vnSet.contains(chars[i + 1])) {
                        addPri(chars[i], chars[i + 2], '=');
                        if (right == false) {

                            return;
                        }
                    }
                    //如果是 aB 即终结符 非终结符 则 a < 该非终极符的 firstVt
                    if (vtSet.contains(chars[i]) && vnSet.contains(chars[i + 1])) {
                        hashSet = firstVtMap.get(chars[i + 1]);
                        for (Character character : hashSet) {
                            addPri(chars[i], character, '<');
                            if (right == false) {
                                return;
                            }
                        }
                    }
                    //如果是 Ba 即非终结符 终结符 则 该非终结符的lastVt > a
                    if (vnSet.contains(chars[i]) && vtSet.contains(chars[i + 1])) {
                        hashSet = lastVtMap.get(chars[i]);
                        for (Character character : hashSet) {
                            addPri(character, chars[i + 1], '>');
                            if (right == false) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 在优先关系表中添加两个终结符之间的优先关系，如果优先关系不唯一，则报错
     *
     * @param x1
     * @param x2
     * @param priority
     */
    public static void addPri(char x1, char x2, char priority) {
        for (int i = 0; i < row; i++) {
            if (x1 == table[i][0]) {
                for (int j = 0; j < col; j++) {
                    if (x2 == table[0][j]) {
                        if (table[i][j] == 0) {
                            table[i][j] = priority;
                        } else if (table[i][j] != priority) {
                            errorMes = table[i][0]+"和"+table[0][j]+"优先关系不唯一，不是算符优先文法";
                            //System.exit(0);
                            right = false;
                            JOptionPane.showMessageDialog(null,errorMes);
                            return;
                        }
                    }
                }

            }
        }
    }

    /**
     * 返回栈中所有字符
     *
     * @return
     */
    static String getStack() {
        String str = "";
        for (Character ch : charStack) {
            str += ch;
        }
        return str;
    }

    /**
     * 进行算符优先文法分析，根据优先关系进行移进和归约，所有归约都用N替换，不做查找对应产生式操作
     */
    public static void analyze() {
        charStack.push('#');//符号栈
        int k = 0;
        int num = 0;
        char[] input = inString.toCharArray();
        steps.add(new Step(num, getStack(), inString, "预备"));
        int pointer = 0;
        num++;
        int j;
        char Q;
        while (true) {
            k = charStack.size() - 1;//栈顶
            if (vtSet.contains(charStack.get(k))) {//找到栈中最顶部的终结符
                j = k;
            } else {
                j = k - 1;
            }
            //找到栈顶终结符和当前输入符号之间的优先关系
            char pri = getPriority(charStack.get(j), input[pointer]);
            if (pri == '>') {//如果优先关系是>就循环在符号栈中找到一个次栈顶终结符并且该终结符和栈顶
                while (true) {
                    Q = charStack.get(j);
                    if (vtSet.contains(charStack.get(j - 1))) {//找到下一个终结符
                        j = j - 1;
                    } else {
                        j = j - 2;
                    }
                    char pri2 = getPriority(charStack.get(j), Q);
                    boolean sign = false;
                    if (pri2 == '<') {//找到可归约串
                        String str = getPartlyStack(j + 1);//取出可归约的符号串
                        charStack.push('N');
                        steps.add(new Step(num, getStack(), inString.substring(pointer), "归约" + 'N' + "->" + str));
                        num++;
                        break;
                    }

                }
            } else if (pri == '<') {//如果优先关系为<则移进
                charStack.push(input[pointer]);
                k++;
                pointer++;
                steps.add(new Step(num, getStack(), inString.substring(pointer), "移进"));
                num++;
            } else if (pri == '=') {
                if (charStack.get(j) == '#') {
                    //System.out.println("分析结束");
                    steps.add(new Step(num, getStack(), inString.substring(pointer), "接受"));
                    num++;
                    return;
                } else {
                    charStack.push(input[pointer]);
                    k++;
                    pointer++;
                    steps.add(new Step(num, getStack(), inString.substring(pointer), "移进"));
                    num++;
                }
            } else {
                //System.out.println("出错");
                errorMes="analyze找不到优先关系";
                JOptionPane.showMessageDialog(null,errorMes);
                right = false;
                return;
            }
        }

    }

    /**
     * 取出栈中可归约的字符串
     *
     * @param i
     * @return
     */
    public static String getPartlyStack(int i) {
        String str = "";
        int size = charStack.size();
        for (int j = i; j < size; j++) {
            str += charStack.get(j);
        }
        for (int j = i; j < size; j++) {
            charStack.pop();
        }
        return str;
    }

    public static void main(String[] args) {
        init();
        getFirstVt();
        getLastVt();
        creatTable();
        for (int i = 0; i < table.length ; i++) {
            for (int j = 0; j < table[0].length; j++) {
                System.out.print(table[i][j]+"\t");
            }
            System.out.println();
        }
        analyze();
        for (int i = 0; i < steps.size(); i++) {
            System.out.println(steps.get(i));
        }

    }

}
