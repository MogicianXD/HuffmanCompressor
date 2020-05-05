package util;

//因为java不支持泛型数组（涉及到类型擦除）
//所以排除使用链式结构的ArrayList后
//只能使用Object[]声明，每次比较都要(Comparable<T>)一次进行强制转换，太麻烦了
//所以这里自定义的类，仅供项目中的HFNode使用
//如果使用STL的PriorityQueue，则需要用Collection或者Set存放用来初始化的数组

public class HFMMinHeap
{
    private int currentSize, maxSize;
    private HFNode[] heap;

    public HFMMinHeap(int minHeapSize) {// 构造函数
        maxSize = minHeapSize;
        heap = new HFNode[maxSize + 1];
        currentSize = 0;
    }

    public int Size() {
        return currentSize;
    }

    public HFNode Min() throws Exception {
        if (currentSize == 0)
            throw new Exception();
        return heap[1];
    }

    public void Insert(HFNode x){
        if (currentSize == maxSize)
            try
            {
                throw new Exception();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        //为x寻找相应插入位置
        //i从新的叶节点开始，并沿着树上升
        int i = ++currentSize;
        while (i != 1 && x.weight < heap[i / 2].weight) {
            //不能把x放入heap[i]
            heap[i] = heap[i / 2];
            i /= 2;
        }
        heap[i] = x;
    }

    public HFNode DeleteMin() {
        if (currentSize == 0)
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
        HFNode x = heap[1];

        HFNode y = heap[currentSize--];//最后一个元素
        //从根开始，为y寻找合适的位置
        int i = 1, //堆的当前节点
                ci = 2;//i的孩子
        while (ci <= currentSize) {
            //选择i孩子中较小的那个
            if (ci < currentSize && heap[ci].weight > heap[ci + 1].weight)
                ci++;
            //能把y放入heap[i]
            if (y.weight <= heap[ci].weight) break;
            //不能
            heap[i] = heap[ci];
            i = ci;
            ci *= 2;
        }
        heap[i] = y;
        return x;
    }

    public void Initialize(HFNode a[], int size, int arraySize) {
        heap = null;
        currentSize = size;
        maxSize = arraySize;
        heap = new HFNode[arraySize + 1];
        //顺便先判断数组是否已经为顺序
        boolean isSorted = true;
        for (int i = 0; i < size - 1; i++) {
            heap[i + 1] = a[i];
            if (isSorted && a[i].weight > a[i + 1].weight)
                isSorted = false;
        }
        heap[size] = a[size - 1];

        if(isSorted)
            return;

        for (int i = currentSize / 2; i >= 1; i--) {
            HFNode y = heap[i]; //子树的根
            //寻找放置y的位置
            int c = 2 * i; //c的父节点是y的目标位置
            while (c <= currentSize) {
                //选择较小的孩子
                if (c < currentSize && heap[c].weight > heap[c + 1].weight)
                    c++;
                //能把y放入heap[c/2]
                if (y.weight <= heap[c].weight)
                    break;
                //不能
                heap[c / 2] = heap[c]; //将孩子上移
                c *= 2; //下移一层
            }
            heap[c / 2] = y;
        }
    }
}