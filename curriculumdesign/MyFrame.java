package curriculumdesign;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Set;
import java.util.Vector;

/**
 * @author 李天翔
 * @date 2022/05/20
 **/
public class MyFrame extends JFrame {

    static JPanel MyPanel;
    static Vector<String> column ;
    static JTable table;
    static JTable firstVtTable;
    static JTable lastVtTable;
    static JTable priTable;

    MyFrame(String title) {

        super(title);
        MyPanel = new JPanel();
        setSize(740, 1000);
        setResizable(false);
        MyPanel.setLayout(null);
        column = new Vector<>();
        column.add("步骤");
        column.add("符号栈");
        column.add("剩余输入串");
        column.add("动作");
        table = new JTable(new Vector<String>(), column);
        column = new Vector<String>();
        column.add("非终结符");
        column.add("firstVt");
        firstVtTable = new JTable(new Vector<String>(), column);
        column = new Vector<String>();
        column.add("非终结符");
        column.add("lastVt");
        lastVtTable = new JTable(new Vector<String>(), column);

        //area.setText("i+i*i#");
        JScrollPane scrollPanel1 = new JScrollPane(table);
        JScrollPane scrollPanel4 = new JScrollPane(firstVtTable);
        JScrollPane scrollPanel5 = new JScrollPane(lastVtTable);
        JScrollPane scrollPanel6 = new JScrollPane(priTable);
        MyPanel.add(scrollPanel1);
        MyPanel.add(scrollPanel4);
        MyPanel.add(scrollPanel5);
        MyPanel.add(scrollPanel6);
        scrollPanel1.setBounds(20, 450, 680, 450);
        scrollPanel4.setBounds(20, 20, 300, 150);
        scrollPanel5.setBounds(400, 20, 300, 150);
        scrollPanel6.setBounds(20, 200, 680, 200);
        this.add(MyPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

}

class InputFrame extends JFrame {
    static JButton btn = new JButton("算符优先分析");
    static JButton btnDef = new JButton("填充默认文法和输入串");
    static JButton btnClear = new JButton("清空输入");
    static JPanel myPanel = new JPanel();
    static JTextArea inputArea = new JTextArea();

    static JTextArea grammarArea = new JTextArea();

    public static void main(String[] args) {
        new InputFrame("算符优先分析界面");
    }

    InputFrame(String title) {
        super(title);
        setSize(740, 300);
        setResizable(false);
        myPanel.setLayout(null);
        inputArea.setFont(new Font("宋体", Font.PLAIN, 20));
        inputArea.setForeground(Color.gray);
        inputArea.setText("请输入分析的表达式以#结尾");
        inputArea.addFocusListener(new JTextAreaFocusListener(inputArea, "请输入分析的表达式以#结尾"));
        grammarArea.setFont(new Font("宋体", Font.PLAIN, 20));
        grammarArea.setText("请输入算符优先文法以#分隔");
        grammarArea.setForeground(Color.gray);
        grammarArea.addFocusListener(new JTextAreaFocusListener(grammarArea, "请输入算符优先文法以#分隔"));
        //area.setText("i+i*i#");
        JScrollPane scrollPanel2 = new JScrollPane(inputArea);
        JScrollPane scrollPanel3 = new JScrollPane(grammarArea);
        myPanel.add(btn);
        myPanel.add(btnDef);
        myPanel.add(btnClear);
        myPanel.add(scrollPanel2);
        myPanel.add(scrollPanel3);
        btnClear.setBounds(275, 170, 200, 30);
        btnDef.setBounds(20, 170, 200, 30);
        btn.setBounds(550, 170, 150, 30);
        scrollPanel2.setBounds(400, 5, 300, 150);
        scrollPanel3.setBounds(20, 5, 300, 150);
        btn.addActionListener(new Listener());
        btnDef.addActionListener(new Listener());
        btnClear.addActionListener(new Listener());
        this.add(myPanel);
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}

class Listener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent actionEvent) {

        if (actionEvent.getSource() == InputFrame.btn) {
            try {
                Priority.inString = InputFrame.inputArea.getText();
                Priority.grammar = InputFrame.grammarArea.getText().replace(" ", "").split("#");
                System.out.println(InputFrame.inputArea.getText());
                System.out.println(InputFrame.grammarArea.getText());
                Priority.init();
                Priority.getFirstVt();
                Priority.getLastVt();
                Priority.creatTable();
                Priority.analyze();
                if (Priority.right == false) {
                    return;
                }
                Vector<Character> v = new Vector<>();
                for (int i = 0; i < Priority.table.length; i++) {
                    v.add(Priority.table[0][i]);

                }
                System.out.println(v.size());
                MyFrame.priTable = new JTable(new Vector(), v);
                new MyFrame("数据展示");
                for (int i = 1; i < Priority.table.length; i++) {
                    Object[] tab = new Object[Priority.table.length];
                    for (int j = 0; j < Priority.table[i].length; j++) {
                        tab[j] = Priority.table[i][j];
                        //System.out.println(tab[j]);
                    }
                    ((DefaultTableModel) MyFrame.priTable.getModel()).addRow(tab);
                }
                for (int i = 0; i < Priority.steps.size(); i++) {
                    Object[] o = new Object[4];
                    o[0] = Priority.steps.get(i).num;
                    o[1] = Priority.steps.get(i).charStack;
                    o[2] = Priority.steps.get(i).input;
                    o[3] = Priority.steps.get(i).action;
                    //System.out.println(Priority.steps.get(i));
                    //System.out.println(o);
                    ((DefaultTableModel) MyFrame.table.getModel()).addRow(o);
                }
                Set<Character> vnSet = Priority.firstVtMap.keySet();
                for (Character character : vnSet) {
                    Object[] o = new Object[2];
                    o[0] = character;
                    o[1] = Priority.firstVtMap.get(character);
                    ((DefaultTableModel) MyFrame.firstVtTable.getModel()).addRow(o);
                }
                for (Character character : vnSet) {
                    Object[] o = new Object[2];
                    o[0] = character;
                    o[1] = Priority.lastVtMap.get(character);
                    ((DefaultTableModel) MyFrame.lastVtTable.getModel()).addRow(o);
                }
            } catch (Exception e) {
                System.out.println(e);
                Priority.table = null;
                JOptionPane.showMessageDialog(null, "出错->" + e);
                return;
            }
        } else if (actionEvent.getSource() == InputFrame.btnDef) {
            InputFrame.inputArea.setText("i+i#");
            InputFrame.grammarArea.setText("E->E+T|T#T->T*F|F#F->(E)|i");
        }else if(actionEvent.getSource() == InputFrame.btnClear){
            InputFrame.inputArea.setText("");
            InputFrame.grammarArea.setText("");
        }
    }
}

class JTextAreaFocusListener implements FocusListener {
    JTextArea jTextArea;
    String hint;

    JTextAreaFocusListener(JTextArea jTextArea, String hint) {
        this.jTextArea = jTextArea;
        this.hint = hint;
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (jTextArea.getText().equals(hint)) {
            jTextArea.setForeground(Color.black);
            jTextArea.setText("");
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (jTextArea.getText().isEmpty()) {
            jTextArea.setForeground(Color.GRAY);
            jTextArea.setText(hint);
        }
    }
}


