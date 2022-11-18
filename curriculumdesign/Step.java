package curriculumdesign;

/**
 * @author 李天翔
 * @date 2022/06/14
 **/
public class Step {
    int num;
    String charStack;
    String input;
    String action;
    public Step(int num, String charStack, String input, String action) {
        this.num = num;
        this.charStack = charStack;
        this.input = input;
        this.action = action;
    }
    @Override
    public String toString() {
        return  num +
                "\t\t" + charStack +
                "\t\t" + input +
                "\t\t" + action;
    }
}
