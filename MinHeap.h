#pragma once
#include <exception>

//最小堆
template<class T>
class MinHeap {
public:
	MinHeap(int MinHeapSize = 10);
	~MinHeap() { delete[] heap; }
	int Size() const { return CurrentSize; }
	T Min() {
		if (CurrentSize == 0) throw;
		return heap[1];
	}
	MinHeap<T>& Insert(const T& x);
	MinHeap<T>& DeleteMin(T& x);
	void Initialize(T a[], int size, int ArraySize);
	void LevelOrder();
private:
	int CurrentSize, MaxSize;
	T *heap;
	// 元素数组
};

template<class T>
MinHeap<T>::MinHeap(int MinHeapSize)
{// 构造函数
	MaxSize = MinHeapSize;
	heap = new T[MaxSize + 1];
	CurrentSize = 0;
}

template<class T>
MinHeap<T>& MinHeap<T>::Insert(const T& x)
{
	if (CurrentSize == MaxSize)
		throw;
	//为x寻找相应插入位置
	//i从新的叶节点开始，并沿着树上升
	int i = ++CurrentSize;
	while (i != 1 && x < heap[i / 2]) 
	{
		//不能把x放入heap[i]
		heap[i] = heap[i / 2];
		i /= 2;
	}
	heap[i] = x;
	return *this;
}

template<class T>
MinHeap<T>& MinHeap<T>::DeleteMin(T& x)
{
	if (CurrentSize == 0)
		throw;
	x = heap[1];

	T y = heap[CurrentSize--];//最后一个元素
	//从根开始，为y寻找合适的位置
	int i = 1, //堆的当前节点
		ci = 2;//i的孩子
	while (ci <= CurrentSize) 
	{
		//选择i孩子中较小的那个
		if (ci < CurrentSize && heap[ci] > heap[ci + 1])
			ci++;
		//能把y放入heap[i]
		if (y <= heap[ci]) break;
		//不能
		heap[i] = heap[ci];
		i = ci;
		ci *= 2;
	}
	heap[i] = y;
	return *this;
}

template<class T>
void MinHeap<T>::Initialize(T a[], int size, int ArraySize)
{
	delete[] heap;
	CurrentSize = size;
	MaxSize = ArraySize;
	heap = new T[ArraySize + 1];
	//顺便先判断数组是否已经为顺序
	bool isSorted = true;
	for (int i = 0; i < size - 1; i++)
	{
		heap[i + 1] = a[i];
		if (isSorted && a[i] > a[i + 1])
			isSorted = false;
	}
	heap[size] = a[size - 1];	
	if (isSorted)
		return;

	for (int i = CurrentSize / 2; i >= 1; i--) 
	{
		T y = heap[i]; //子树的根
		//寻找放置y的位置
		int c = 2 * i; //c的父节点是y的目标位置
		while (c <= CurrentSize) 
		{
			//选择较小的孩子
			if (c < CurrentSize && heap[c] > heap[c + 1]) 
				c++;
			//能把y放入heap[c/2]
			if (y <= heap[c])
				break;
			//不能
			heap[c/2] = heap[c]; //将孩子上移
			c *= 2; //下移一层
		}
		heap[c/2] = y;
	}
}

//template <class T>
//void MinHeap<T>::LevelOrder()
//{
//	for (int i = 1; i < CurrentSize; i++)
//	{
//		cout << heap[i] << ",";
//	}
//	cout << heap[CurrentSize] << endl;
//}