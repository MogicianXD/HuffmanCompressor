public class Test
{
    public static void main(String args[])
    {
        int fileNum = 526;
        System.out.println((byte)((fileNum<<24)>>24));
    }
}
