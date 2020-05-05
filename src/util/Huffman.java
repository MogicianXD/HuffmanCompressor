package util;

import controller.ProgressController;
import controller.ReplaceController;
import entity.MyFile;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import service.HFMTask;

import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

class HFNode implements Comparable<HFNode>
{
    int id;
    long weight;
    String code;
    HFNode leftChild;
    HFNode rightChild;

    HFNode()
    {
        id = -1;
        weight = 0;
        code = "";
        leftChild = rightChild = null;
    }

	HFNode(int c, long w)
    {
        id = c;
        weight = w;
        code = "";
        leftChild = rightChild = null;
    }

	HFNode(int c, long w, HFNode l,HFNode r)
    {
        id = c;
        weight = w;
        code = "";
        leftChild = l;
        rightChild = r;
    }

    @Override
    public int compareTo(HFNode node) {
        return (int)(weight - node.weight);
    }
}

public class Huffman
{
    private final int MAX_SIZE = 257;
    private final int CODE_BUFF_SIZE = 800;
    private final int WRITE_BUFF_SIZE = 1024*32;
    private final int READ_BUFF_SIZE = 1024*32;
    private final int PSEUDO_EOF = 256;
    private int size;
    private long in_file_size = 0L;
    private int mark;
    private int read_num;
    private int file_count = 0;
    private int fileNum = 1;
    private String[] dictionary;
    private long now;
    private byte[] read_buf;
    private BufferedInputStream in_file;
    private BufferedOutputStream out_file;
    private String filename;
//    private HashMap<Integer, String> table;
    private String[] table;
    private HFNode root;
    private String hftree_path;
    private boolean paused = false;

    public Huffman()
    {
        size = 0;
        root = null;
        task = new HFMTask();
    }

    public void pause()
    {
        paused = true;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public void resume() { paused = false; }

    private void buildTree()
    {
        long[] freq = new long[MAX_SIZE];
        getFreq(freq);
        initialize(freq);
    }

    private void getFreq(long[] freq)
    {
        try
        {
            byte in_char = 0;
            read_num = in_file.read(read_buf);
            // 依次读入字符，统计数据
            while (read_num != -1)
            {
                in_char = read_buf[mark++];
                freq[in_char&0xff]++;
                bufCheck();
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void initialize(long[] freq)
    {
        size = 0;
        HFNode[] node = new HFNode[MAX_SIZE];
        for (int i = 0; i < MAX_SIZE; i++)
            if (freq[i] != 0)
                node[size++] = new HFNode(i, freq[i]);

        // 插入频率为1的pseudo-EOF
        node[size++] = new HFNode(PSEUDO_EOF, 1);

        // 把数组变成一个最小堆
        HFMMinHeap H = new HFMMinHeap(1);
        H.Initialize(node, size, size);
        //不断取出最小的两个，然后将合并的结果插入
        HFNode x, y, z = new HFNode();
        for (int i = 1; i < size; i++) {
            x = new HFNode();
            y = new HFNode();
            x = H.DeleteMin();
            y = H.DeleteMin();
            z = new HFNode(-1, x.weight + y.weight, x, y);
            H.Insert(z);
        }
        this.root = z;
        node = null;
        encodeTranversal();
    }

    private void encodeTranversal()
    {
//        table = new HashMap<>();
        table = new String[MAX_SIZE];
        //层次遍历，定义节点的译码，并保存外部节点的map映射
        Queue<HFNode> q = new LinkedList<HFNode>();
        if (root == null)
            return;
        q.offer(root);
        while (!q.isEmpty()) {
            for (int i = 0; i < q.size(); i++) {
                HFNode p = q.poll();
                if (p.leftChild != null) {
                    q.offer(p.leftChild);
                    p.leftChild.code = p.code + '0';
                }

                if (p.rightChild != null) {
                    q.offer(p.rightChild);
                    p.rightChild.code = p.code + '1';
                }

                if (p.id != -1)
//                    table.put(p.id, p.code);
                    table[p.id] = p.code;
            }
        }
    }

    public void setInPath(final String in_file_path)
    {
        filename = in_file_path.substring(in_file_path.lastIndexOf(File.separator) + File.separator.length());
        File in = new File(in_file_path);
        in_file_size = in.length();
        fileNum = 1;
        dictionary = new String[fileNum];
        dictionary[0] = filename;
        setInPath(in_file_path, in_file_size);
    }

    public void setOutPath(File outFile)
    {
        try
        {
            out_file = new BufferedOutputStream(new FileOutputStream(outFile));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setOutPath(final String out_file_path) {
        try
        {
            out_file = new BufferedOutputStream(new FileOutputStream(out_file_path));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void setInPath(final String in_file_path, long in_file_size)
    {
        try
        {
            this.in_file_size = in_file_size;
            in_file = new BufferedInputStream(new FileInputStream(in_file_path));
            read_buf = new byte[READ_BUFF_SIZE];
            mark = 0;
            read_num = 0;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private HFMTask task;

    public HFMTask setTask(HFMTask t)
    {
        task = t;
        task.updateProgressIndex(0);
        task.updateLog("正在读取文件");
        return task;
    }

    public void compress(ObservableList<TreeItem<MyFile>> files, final int fileNum, final String out_file_path)
    {
        try
        {
            this.fileNum = fileNum;
            String[] absolutePaths = new String[fileNum];
            dictionary = new String[fileNum];
            in_file_size = 0;
            file_count = 0;
            now = 0;
            getPaths(files, absolutePaths, -1);
            if(file_count != fileNum)
                    throw new Exception();

            setOutPath(out_file_path);
            writeDictionary(fileNum);
            disposeDictionary();

            //建树，并输出映射表
            long[] freq = new long[MAX_SIZE];
            read_buf = new byte[READ_BUFF_SIZE];
            mark = 0;
            for(int i = 0; i < fileNum; i++)
            {
                if(paused)
                {
                    if(task.isCancelled())
                    {
                        File hfmFile = new File(out_file_path);
                        hfmFile.delete();
                        return;
                    }
                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                String path = absolutePaths[i];
                if(path.endsWith(File.separator))
                    continue;
                in_file = new BufferedInputStream(new FileInputStream(path));
                getFreq(freq);
            }
            initialize(freq);
            outputHFTree();

            for(int i = 0; i < fileNum; i++)
            {
                //文件夹不编译
                if(dictionary[i].endsWith(File.separator))
                    continue;
                String log = dictionary[i];
                if(log.endsWith(File.separator))
                    log = log.substring(0, log.length()-File.separator.length());
                task.updateLog("正在压缩："+ log);
                setInPath(absolutePaths[i], in_file_size);
                code();
                if(task.isCancelled())
                {
                    closeFile();
                    File hfmFile = new File(out_file_path);
                    hfmFile.delete();
                    return;
                }
            }

            task.updateProgressIndex(100);
            System.out.print("\r编码中......[100%]\n");
            closeFile();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getPaths(ObservableList<TreeItem<MyFile>> files, String[] absolutePaths, int parentIndex)
    {
        for(TreeItem<MyFile> file : files)
        {
            String str = file.getValue().getPath();
            in_file_size += file.getValue().getLength();
            absolutePaths[file_count] = str;
            if(parentIndex == -1)
                dictionary[file_count++] = str.substring(
                        str.lastIndexOf(File.separator, str.length()-2)+File.separator.length(), str.length());
            else
                dictionary[file_count++] = "<" + parentIndex + ">" + str.substring(
                        str.lastIndexOf(File.separator, str.length()-2), str.length());
            if(!file.isLeaf())
            {
                getPaths(file.getChildren(), absolutePaths, file_count - 1);
            }
        }
    }

    public void compress(final String in_file_path, final String out_file_path) {
        setInPath(in_file_path);
        buildTree();
        setInPath(in_file_path);
        setOutPath(out_file_path);
        writeDictionary();
        outputHFTree();
        code();
        closeFile();
    }

    private void outputHFTree()
    {
        if (root == null)
            try
            {
                throw new Exception();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        try
        {
            int count = 0;
            byte[] write_buf = new byte[WRITE_BUFF_SIZE];
            // 第1、2字节写入节点数（byte）
            write_buf[count++] = (byte)(size / 256);
            write_buf[count++] = (byte)(size % 256);
            byte out_c = 0, tmp_c = 1;
            // 接下来写入huffman树，每行写入字符+编码长度+huffman编码的二进制(低位0补齐）
            for(int i = 0; i < MAX_SIZE; i++)
                if(table[i] != null)
                {
                    int length =  table[i].length();
                    if(i != PSEUDO_EOF)
                        write_buf[count++] = (byte)i;
                    write_buf[count++] = (byte)length;
                    for (int j = 0; j < length; j++) {
                        if ('1' == table[i].charAt(j))
                            out_c += tmp_c << (7 - (j % 8));
                        if (0 == (j + 1) % 8 || j == length - 1) {
                            // 每8位写入一次文件
                            write_buf[count++] = out_c;
                            out_c = 0;
                        }
                    }
                }
            out_file.write(write_buf, 0, count);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void writeDictionary(int fileNum)
    {
        try
        {
            //先写文件数和文件名
            out_file.write((byte)(fileNum>>24));
            out_file.write((byte)((fileNum<<8)>>24));
            out_file.write((byte)((fileNum<<16)>>24));
            out_file.write((byte)((fileNum<<24)>>24));
            for(int i = 0; i < fileNum; i++)
            {
                byte[] bs = dictionary[i].getBytes();
                out_file.write((byte)(bs.length/256));
                out_file.write((byte)(bs.length%256));
                out_file.write(bs);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void writeDictionary()
    {
        //先写文件数和文件名
        try
        {
            out_file.write(0);
            out_file.write(0);
            out_file.write(0);
            out_file.write(1);
            byte[] bs = filename.getBytes();
            out_file.write((byte)(bs.length/256));
            out_file.write((byte)(bs.length%256));
            out_file.write(bs);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void code()
    {
        String progress_text;
        try
        {
            int length, i, j;
            int count = 0;
            byte in_char;
            byte out_c = 0, tmp_c = 1;
            String code_string;
            byte[] write_buf = new byte[WRITE_BUFF_SIZE];

            // 写入huffman编码
            code_string = "";
            bufCheck();
            while (read_num != -1)
            {
                if(paused)
                {
                    if(task.isCancelled())
                        return;
                    synchronized (this)
                    {
                        try
                        {
                            this.wait();
                        } catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                in_char = read_buf[mark++];
                bufCheck();
                now++;
                // 找到每一个字符所对应的huffman编码
                try
                {
                    code_string += table[in_char&0xff];
                }
                catch (Exception e)
                {
                    System.err.println("字典中可能没有对应的编码");
                    e.printStackTrace();
                }
                // 当总编码的长度大于预设的CODE_BUFF_SIZE时再写入文件
                length = code_string.length();
                if (length > CODE_BUFF_SIZE) {

                    //确保out_string中没有残存的char
                    for (i = 0; i + 7 < length; i += 8) {
                        out_c = 0;
                        for (j = 0; j < 8; j++) {
                            if ('1' == code_string.charAt(i+j))
                                out_c += tmp_c << (7 - j);
                        }
                        write_buf[count++] = out_c;
                        if(count == WRITE_BUFF_SIZE)
                        {
                            out_file.write(write_buf, 0, WRITE_BUFF_SIZE);
                            count = 0;
                        }
                    }
                    code_string = code_string.substring(i, length);

                    int progressIndegree = (int) (100 * now / in_file_size);
                    task.updateProgressIndex(progressIndegree);
                    System.out.print("\r编码中......[" + progressIndegree + "%]");
                }
            }

            // 已读完所有文件，先插入pseudo-EOF
            try
            {
//                code_string += table.get(PSEUDO_EOF);
                code_string += table[PSEUDO_EOF];
            }
            catch (Exception e)
            {
                System.err.println("字典中可能没有对应的编码");
                e.printStackTrace();
            }
            // 再处理尾部剩余的huffman编码
            length = code_string.length();
            out_c = 0;
            for (i = 0; i < length; i++) {
                if ('1' == code_string.charAt(i))
                    out_c += tmp_c << (7 - (i % 8));
                if (0 == (i + 1) % 8 || i == length - 1) {
                    // 每8位写入一次文件
                    write_buf[count++] = out_c;
                    if(count == WRITE_BUFF_SIZE)
                    {
                        out_file.write(write_buf, 0, WRITE_BUFF_SIZE);
                        count = 0;
                    }
                    out_c = 0;
                }
            }
            if(count != 0)
                out_file.write(write_buf,0, count);
            in_file.close();
            System.out.println("\r编码中......[100%]");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void decompress(final String in_file_path, final String out_root_path)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        setInPath(in_file_path);
        readDictionary();
        rebuildTree();
        boolean[] fileExists = new boolean[fileNum];
        for(file_count = 0; file_count < fileNum; file_count++)
        {
            if(dictionary[file_count].charAt(0) == '<')
            {
                dictionary[file_count] = dictionary[Integer.parseInt(
                        dictionary[file_count].substring(1, dictionary[file_count].indexOf('>')))]
                        + File.separator
                        + dictionary[file_count].substring(dictionary[file_count].indexOf('>')+File.separator.length()+1);
            }
            String path = dictionary[file_count];
            boolean isDictionary = false;
            if(path.endsWith(File.separator))
            {
                isDictionary = true;
                path = path.substring(0, path.length() - File.separator.length());
                dictionary[file_count] = path;
            }

            File file = new File(out_root_path + File.separator + path);
            if((fileExists[file_count] = file.exists()))
            {
                task.replaceAsk(file.getAbsolutePath());
                ReplaceController.Mode mode = task.getMode();
                switch (mode)
                {
                    case CANCEL:
                        for(int i = file_count-1; i >= 0; i--)
                        {
                            if(fileExists[i])
                                continue;
                            file = new File(out_root_path + File.separator + dictionary[i]);
                            file.delete();
                        }
                        return;
                    case RESERVE:
                        int suffix = 2, index;
                        String str1, str2;
                        if((index = path.indexOf('.')) == -1)
                        {
                            str1 = path;
                            str2 = "";
                        }
                        else
                        {
                            str1 = path.substring(0, index);
                            str2 = path.substring(index);
                        }
                        while(true)
                        {
                            path = str1 + "(" + suffix++ + ")" + str2;
                            dictionary[file_count] = path;
                            file = new File(out_root_path + File.separator + path);
                            if(!file.exists())
                                break;
                        }
                        break;
                    case REPLACE:
                        break;
                }
            }

            file = new File(out_root_path + File.separator + path);
            if(isDictionary)
            {
                file.mkdir();
                continue;
            }

            task.updateLog("正在解压："+path);

            if(fileExists[file_count])
            {
                //备份，缀以"temp201XXXXXXXXX"（当前时间）
                String backup = "temp" + df.format(new Date());
                File tmpFile = new File(out_root_path + File.separator + path + backup);
                try
                {
                    //设置为隐藏文件
                    String set = "attrib +H " + tmpFile.getAbsolutePath();
                    Runtime.getRuntime().exec(set);

                    setOutPath(tmpFile);
                    decode();

                    if(task.isCancelled())
                        file = tmpFile;
                    else
                    {
                        //必须把原文件删除掉才能移动
                        file.delete();
                        tmpFile.renameTo(file);
                        set = "attrib -H " + file.getAbsolutePath();
                        Runtime.getRuntime().exec(set);
                    }

                } catch (IOException e)
                {
                    if(tmpFile.exists())
                        tmpFile.delete();
                    e.printStackTrace();
                }
            }
            else
            {
                setOutPath(file);
                decode();
            }

            if(task.isCancelled())
            {
                closeFile();
                file.delete();
                for(int i = file_count-1; i >= 0; i--)
                {
                    if(fileExists[i])
                        continue;
                    file = new File(out_root_path + File.separator + dictionary[i]);
                    file.delete();
                }
                return;
            }
        }
        closeFile();
        task.updateProgressIndex(100);
        System.out.print("\r解码中......[100%]\n");
    }

    private void readDictionary()
    {
        try
        {
            read_num = in_file.read(read_buf);
            fileNum = 0;
            for(mark = 0; mark < 4; mark++)
            {
                fileNum = fileNum << 8;
                fileNum += (int)read_buf[mark];
            }
            dictionary = new String[fileNum];

            for (int i = 0; i < fileNum; ++i)
            {
                int len = 256*(read_buf[mark++]&0xff);
                len += read_buf[mark++]&0xff;
                byte[] bs = new byte[len];
                if(mark + len < read_num)
                {
                    System.arraycopy(read_buf, mark, bs, 0, len);
                    dictionary[i] = new String(bs);
                    mark += len;
                }
                else
                {
                    int left = read_num - mark;
                    System.arraycopy(read_buf, mark, bs, 0, left);
                    dictionary[i] = new String(bs);
                    read_num = in_file.read(read_buf);
                    bs = new byte[len - left];
                    System.arraycopy(read_buf, mark, bs, 0, len - left);
                    dictionary[i] += new String(bs);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void disposeDictionary()
    {
        for(int i = 0; i < fileNum; i++)
        {
            if(dictionary[i].charAt(0) == '<')
            {
                dictionary[i] = dictionary[Integer.parseInt(
                        dictionary[i].substring(1, dictionary[i].indexOf('>')))]
                        + dictionary[i].substring(dictionary[i].indexOf('>')+File.separator.length()+1);
            }
        }
    }

    public String[] getDictionary(String hfm_file_path)
    {
        setInPath(hfm_file_path);
        readDictionary();
        return dictionary;
    }

    private void decode()
    {
        int i, id, count = 0;
        byte in_char;
        byte flag;
        HFNode node;

        node = root;

        byte[] write_buf = new byte[WRITE_BUFF_SIZE];
        try
        {
            bufCheck();
            outer:while(read_num != -1)
            {
                while(mark < read_num)
                {
                    if(paused)
                    {
                        if(task.isCancelled())
                            return;
                        synchronized (this)
                        {
                            try
                            {
                                this.wait();
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    in_char = read_buf[mark++];
                    now++;
                    flag = (byte)0x80;
                    for (i = 0; i < 8; ++i)
                    {
                        if ((in_char & flag) == 0)
                            node = node.leftChild;
                        else
                            node = node.rightChild;

                        if(node == null)
                            System.out.println("文件损坏");
                        id = node.id;
                        if (id >= 0)
                        {
                            if (id == PSEUDO_EOF)
                                break outer;
                            else
                            {
                                write_buf[count++] = (byte) id;
                                if(count == WRITE_BUFF_SIZE)
                                {
                                    out_file.write(write_buf, 0, WRITE_BUFF_SIZE);
                                    count = 0;
                                }
                                node = root;
                            }
                        }
                        flag = (byte) ((flag&0xff) >> 1);
                    }
                }
                int progressIndegree = (int) (100 * now / in_file_size);
                task.updateProgressIndex(progressIndegree);
                System.out.print("\r解码中......[" + progressIndegree + "%]");

                read_num = in_file.read(read_buf);
                mark = 0;
            }
            if (count > 0)
                out_file.write(write_buf,0, count);
            out_file.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void bufCheck() throws IOException
    {
        if(mark == read_num)
        {
            read_num = in_file.read(read_buf);
            mark = 0;
        }
    }

    private void rebuildTree()
    {
        int i, j, id = -1, length;
        String code = "";
        HFNode node;
        root = null;
        root = new HFNode();

        try
        {
            bufCheck();
            size =  256 * (read_buf[mark++]&0xff);
            size += read_buf[mark++]&0xff;
            bufCheck();

            for (i = 0; i < size; ++i)
            {
                node = root;
                code = "";
                if(i == size - 1)
                    id = PSEUDO_EOF;
                else
                    id = read_buf[mark++]&0xff;
                bufCheck();
                length = read_buf[mark++]&0xff;
                bufCheck();
                byte in_char = 0;
                byte flag = (byte)0x80;
                for(j = 0; j < length; j++)
                {
                    if( j % 8 == 0)
                    {
                        in_char = read_buf[mark++];
                        bufCheck();
                        flag = (byte)0x80;
                    }

                    if (node.id != -1)
                    {
                        //途中访问到的是叶子节点，说明寻路到了其他字符的编码处，huffman编码不对
                        System.err.println("文件头译码格式不正确，文件损坏.");
                        throw new Exception();
                    }
                    if ((in_char & flag) == 0)
                    {
                        code += 0;
                        if (node.leftChild == null)
                            node.leftChild = new HFNode();
                        else if (j == length - 1)
                        {
                            System.err.println("文件头译码格式不正确，文件损坏.");
                            throw new Exception();
                        }
                        node = node.leftChild;
                    }
                    else
                    {
                        code += 1;
                        if (node.rightChild == null)
                            node.rightChild = new HFNode();
                        else if (j == length - 1)
                        {
                            System.err.println("文件头译码格式不正确，文件损坏.");
                            throw new Exception();
                        }
                        node = node.rightChild;
                    }
                    flag = (byte) ((flag&0xff) >> 1);
                }
                node.id = id;
                node.code = code;
            }
            encodeTranversal();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void inOrderVisit(HFNode node, int pos, String[] list) {
        if (node == null)
            return;
        inOrderVisit(node.leftChild, pos * 2, list);
        if (node.id != -1)
            list[pos] = node.id + "";
        else
        list[pos] = "XXX";
        inOrderVisit(node.rightChild, pos * 2 + 1, list);
    }

    public void printTree(String out_file_path) {
        setOutPath(out_file_path);

        int height = 0;
//        for (HashMap.Entry<Integer, String> entry : table.entrySet())
//            if (height < entry.getValue().length())
//                height = entry.getValue().length();
        for(int i = 0; i < MAX_SIZE; i++)
            if(table[i] != null && height < table[i].length())
                height = table[i].length();
        height++;

        int i, j, k, l, pos;
        String str;
        int max_size = (int)Math.pow(2, height);
        String[] list = new String[max_size];
        for (i = 0; i < max_size; i++)
            list[i] = "   ";
        list[1] = "XXX";

        inOrderVisit(root, 1, list);

        try
        {
            byte[] temp = {'0','0','0'};
            int s = (int)Math.pow(2, height) - 1;
            int h = height;
            for (i = 1; i <= s; i++) {
                for (j = 0; j < (int)Math.pow(2, h - 1) - 1; j++)
                    out_file.write("  ".getBytes());
                for(j = 0; j < list[i].length(); j++)
                    temp[list[i].length() - j - 1] = (byte)list[i].charAt(j);
                out_file.write(temp);
                for (j = 0; j < (int)Math.pow(2, h - 1) - 1; j++)
                    out_file.write("  ".getBytes());
                out_file.write(' ');
                if (i == (int)Math.pow(2, height - h + 1) - 1) {
                    out_file.write('\n');
                    if (i == s)
                        break;
                    for (k = i + 1; k <= 2 * i + 1; k++) {
                        if (list[k] == "   ")
                            for (l = 0; l < (int)Math.pow(2, h) - 1; l++)
                                out_file.write(' ');
                        else {
                            if (k % 2 == 0) {
                                for (l = 0; l < (int)Math.pow(2, h - 1); l++)
                                    out_file.write(' ');
                                for (l = 0; l < (int)Math.pow(2, h - 1) - 2; l++)
                                    out_file.write('_');
                                out_file.write('/');
                            } else {
                                out_file.write('\\');
                                for (l = 0; l < (int)Math.pow(2, h - 1) - 2; l++)
                                    out_file.write('_');
                                for (l = 0; l < (int)Math.pow(2, h - 1); l++)
                                    out_file.write(' ');
                            }
                        }
                        out_file.write(' ');
                    }

                    out_file.write('\n');

                    for (k = i + 1; k <= 2 * i + 1; k++) {
                        if (list[k] == "   ")
                            for (l = 0; l < (int)Math.pow(2, h) - 1; l++)
                                out_file.write(' ');
                        else {
                            if (k % 2 == 0) {
                                for (l = 0; l < (int)Math.pow(2, h - 1) - 1; l++)
                                    out_file.write(' ');
                                out_file.write('/');
                                for (l = 0; l < (int)Math.pow(2, h - 1) - 1; l++)
                                    out_file.write(' ');
                            } else {
                                for (l = 0; l < (int)Math.pow(2, h - 1) - 1; l++)
                                    out_file.write(' ');
                                out_file.write('\\');
                                for (l = 0; l < (int)Math.pow(2, h - 1) - 1; l++)
                                    out_file.write(' ');
                            }
                        }
                        out_file.write(' ');
                    }
                    out_file.write('\n');
                    h--;
                }
            }
            closeFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void closeFile()
    {
        try
        {
            in_file.close();
            out_file.close();
            read_num = 0;
            mark = 0;
            now = 0;
            read_buf = null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean treeIsBuilt()
    {
        return (root != null);
    }

    public void readWithTree(final String hftree_path, final String in_file_path, final String out_file_path) {
        rebuildBy(hftree_path);
        readWithBuiltTree(in_file_path, out_file_path);
    }

    public void readWithBuiltTree(final String in_file_path, final String out_file_path) {
        setInPath(in_file_path);
        setOutPath(out_file_path);
        decode();
        closeFile();
    }

    private void rebuildBy(final String hftree_path) {
        this.hftree_path = hftree_path;
        setInPath(hftree_path);
        rebuildTree();
    }

    public void WriteCodeWithBuiltTree(final String in_file_path, final String out_file_path)
    {
        if (root == null)
            try
            {
                throw new Exception();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        setInPath(in_file_path);
        setOutPath(out_file_path);
        code();
        closeFile();
    }

    public void writeCode(final String in_file_path, final String out_file_path)
    {
        setInPath(in_file_path);
        buildTree();
        setInPath(in_file_path);
        setOutPath(out_file_path);
        code();
        closeFile();
    }

    public void writeBuiltTree(final String hftree_path) {
        setOutPath(hftree_path);
        outputHFTree();
        closeFile();
    }

    public void writeCodeWithTree(final String hftree_path, final String in_file_path, final String out_file_path)
    {
        rebuildBy(hftree_path);
        setInPath(in_file_path);
        setOutPath(out_file_path);
        code();
        closeFile();
    }

}
