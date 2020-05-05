import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import util.Huffman;

public class Coder
{

    private static String in_file_path, out_file_path, hftree_path;
    private static Huffman hf;
    private static long start;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args)
    {
        hf = new Huffman();
        System.out.println("======================================");
        System.out.println("      欢迎使用霍夫曼编码解码系统");
        System.out.println("======================================");
        System.out.println("(1)以内存中Huffman树编码请输入'c'");
        System.out.println("(2)指定文件构建Huffman树编码请输入'e'");
        System.out.println("(3)以内存中Huffman树解码请输入'r'");
        System.out.println("(4)指定文件构建Huffman树解码请输入'm'");
        System.out.println("(5)打印编码请输入'p'");
        System.out.println("(6)打印赫夫曼树请输入't'");
        System.out.println("(7)压缩请输入'z'");
        System.out.println("(8)解压请输入'd'");
        System.out.println("(9)离开请输入'q'");
        System.out.println("======================================");
        char choice = 'a';
        while (choice != 'q')
        {
            System.out.print("请输入下一步： ");
            choice = scanner.nextLine().charAt(0);
            switch (choice)
            {
                case 'c':
                    Coding();
                    break;
                case 'e':
                    Encoding();
                    break;
                case 'r':
                    Decoding();
                    break;
                case 'm':
                    DeEncoding();
                    break;
                case 'z':
                    Compress();
                    break;
                case 'd':
                    Decompress();
                    break;
                case 'p':
                    PrintCode();
                    break;
                case 't':
                    PrintTree();
                    break;
                case 'q':
                    break;

                default:
                    System.out.println("输入错误");
                    continue;
            }
        }
    }


    private static void set_in_path()
    {
        System.out.println("***输入要读取的文件路径或文件名**");
        in_file_path = scanner.nextLine();
    }

    private static void set_out_path()
    {
        System.out.println("***输入要写入的路径或文件名****");
        out_file_path = scanner.nextLine();
    }

    private static void set_hfmtree_path()
    {
        System.out.println("***输入要使用的霍夫曼树文件名***");
        hftree_path = scanner.nextLine();
    }

    private static void finish()
    {
        System.out.println("成功！");
        long end = System.currentTimeMillis();
        System.out.println("用时" + (double)(end - start) / 1000 + "秒");
    }

    private static void OutputHFTree()
    {
        System.out.println("Tip:是否将该树输出成hfmtree文件？");
        System.out.println("Tip:是，则输入1");
        System.out.println("Tip:否，则输入0");
        System.out.print("请输入： ");
        char flag;
        while (true)
        {
            flag = scanner.nextLine().charAt(0);
            if (flag == '0' || flag == '1')
                break;
            System.out.println("输入格式不正确");
            System.out.print("请重新输入： ");
        }
        if (flag == '0')
            return;
        else
        {
            set_hfmtree_path();
            start = System.currentTimeMillis();
            hf.writeBuiltTree(hftree_path);
            finish();
        }
    }

    private static void Encoding()
    {
        System.out.println("Tip:输入0,则根据待编码文件重建");
        System.out.println("Tip:输入1,再输入hfmtree文件，则根据该hfmtree重建");
        System.out.print("请输入： ");
        char flag;
        while (true)
        {
            flag = scanner.nextLine().charAt(0);
            if (flag == '0' || flag == '1')
                break;
            System.out.println("输入格式不正确");
            System.out.print("请重新输入： ");
        }
        if (flag == '1')
        {
            set_hfmtree_path();
            set_in_path();
            set_out_path();
            start = System.currentTimeMillis();
            hf.writeCodeWithTree(hftree_path, in_file_path, out_file_path);
            finish();
        }
        else
        {
            set_in_path();
            set_out_path();
            start = System.currentTimeMillis();
            hf.writeCode(in_file_path, out_file_path);
            finish();
            OutputHFTree();
        }
    }

    private static void Coding()
    {
        if (hf.treeIsBuilt())
        {
            set_in_path();
            set_out_path();
            start = System.currentTimeMillis();
            hf.WriteCodeWithBuiltTree(in_file_path, out_file_path);
            finish();
            OutputHFTree();
        }
        else
        {
            System.out.println("Err:Huffman树不在内存中，需要重建");
            Encoding();
        }

    }

    private static void Decompress()
    {
        set_in_path();
        set_out_path();
        start = System.currentTimeMillis();
        hf.decompress(in_file_path, out_file_path);
        finish();
    }

    private static void DeEncoding()
    {
        System.out.println("输入hfmtree文件，则根据该hfmtree重建");
        System.out.println("请输入： ");
        set_hfmtree_path();
        set_in_path();
        set_out_path();
        start = System.currentTimeMillis();
        hf.readWithTree(hftree_path, in_file_path, out_file_path);
        finish();
    }

    private static void Decoding()
    {
        if (hf.treeIsBuilt())
        {
            set_in_path();
            set_out_path();
            start = System.currentTimeMillis();
            hf.readWithBuiltTree(in_file_path, out_file_path);
            finish();
        }
        else
        {
            System.out.println("Err:Huffman树不在内存中，需要重建");
            DeEncoding();
        }

    }

    private static void Compress()
    {
        set_in_path();
        set_out_path();
        start = System.currentTimeMillis();
        for(int i = 0; i < 100; i++)
        hf.compress(in_file_path, out_file_path);
        finish();
    }

    private static void PrintCode()
    {
        try
        {
            set_in_path();
            start = System.currentTimeMillis();
            BufferedInputStream in_file = new BufferedInputStream(new FileInputStream(in_file_path));
            int count = 0;

            //中文会出现乱码，但题目只是打印译码也无所谓了
            byte in_char;
            byte[] buff = new byte[50];
            byte[] bs;
            String str;
            System.out.println("译码文件打印如下：");
            while ((in_char=(byte)in_file.read())!=-1)
            {
                buff[count++] = in_char;
                if (in_char == '\t')
                    count = (count / 8 + 1) * 8;
                if (in_char == '\n' || in_char == '\r')
                {
                    bs = new byte[count];
                    System.arraycopy(buff,0,bs,0, count);
                    System.out.println(new String(bs));
                    count = 0;
                }
                if (count >= 50)
                {
                    bs = new byte[count];
                    System.arraycopy(buff,0,bs,0, count);
                    System.out.println(new String(bs));
                    count = 0;
                }
            }
            in_file.close();
            finish();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void PrintTree()
    {
        set_out_path();
        hf.printTree(out_file_path);
    }

}
