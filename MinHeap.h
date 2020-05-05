#pragma once
#include <exception>

//��С��
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
	// Ԫ������
};

template<class T>
MinHeap<T>::MinHeap(int MinHeapSize)
{// ���캯��
	MaxSize = MinHeapSize;
	heap = new T[MaxSize + 1];
	CurrentSize = 0;
}

template<class T>
MinHeap<T>& MinHeap<T>::Insert(const T& x)
{
	if (CurrentSize == MaxSize)
		throw;
	//ΪxѰ����Ӧ����λ��
	//i���µ�Ҷ�ڵ㿪ʼ��������������
	int i = ++CurrentSize;
	while (i != 1 && x < heap[i / 2]) 
	{
		//���ܰ�x����heap[i]
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

	T y = heap[CurrentSize--];//���һ��Ԫ��
	//�Ӹ���ʼ��ΪyѰ�Һ��ʵ�λ��
	int i = 1, //�ѵĵ�ǰ�ڵ�
		ci = 2;//i�ĺ���
	while (ci <= CurrentSize) 
	{
		//ѡ��i�����н�С���Ǹ�
		if (ci < CurrentSize && heap[ci] > heap[ci + 1])
			ci++;
		//�ܰ�y����heap[i]
		if (y <= heap[ci]) break;
		//����
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
	//˳�����ж������Ƿ��Ѿ�Ϊ˳��
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
		T y = heap[i]; //�����ĸ�
		//Ѱ�ҷ���y��λ��
		int c = 2 * i; //c�ĸ��ڵ���y��Ŀ��λ��
		while (c <= CurrentSize) 
		{
			//ѡ���С�ĺ���
			if (c < CurrentSize && heap[c] > heap[c + 1]) 
				c++;
			//�ܰ�y����heap[c/2]
			if (y <= heap[c])
				break;
			//����
			heap[c/2] = heap[c]; //����������
			c *= 2; //����һ��
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