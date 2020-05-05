#pragma once
#include "MinHeap.h"
#include <string>
#include <queue>
#include <map>
#include <fstream>
#include <math.h>
#include <iostream>
#include <iomanip>

using namespace std;

#define MAX_SIZE 257
#define READ_BUFF_SIZE 100
#define CODE_BUFF_SIZE 20000
#define WRITE_BUFF_SIZE 1024*8
#define PSEUDO_EOF 256

template<class T = unsigned long> class Huffman;
template<class T = unsigned long>
class HFNode
{
private:
	int id;
	T weight;
	string code;
	HFNode<T> *LeftChild;
	HFNode<T> *RightChild;
public:
	friend class Huffman<T>;//ģ������Ԫ�������
	HFNode() { id = -1; weight = 0; code = ""; LeftChild = RightChild = NULL; }
	HFNode<T>(int c, T w) { id = c; weight = w; code = ""; LeftChild = RightChild = NULL; }
	HFNode<T>(int c, T w, HFNode<T> *l, HFNode<T> *r)
		: id(c), weight(w), LeftChild(l), RightChild(r) {}
	inline bool operator<(const HFNode<T> node) const { return weight < node.weight; }
	inline bool operator<=(const HFNode<T> node) const { return weight <= node.weight; }
	inline bool operator>(const HFNode<T> node) const { return weight > node.weight; }
};

template<class T>
class Huffman
{
public:
	Huffman() { root = 0; size = 0; }
	~Huffman() { Deactivate(); in_file.close(); out_file.close(); }
	bool TreeIsBuilt();
	bool inIsOpen();
	void Initialize(HFNode<T> node[], int n);
	void SetInPath(const string in_file_name) throw (string);
	void SetOutPath(const string out_file_name);
	void Compress(const string in_file_name, const string out_file_name) throw (string);
	void WriteCodeWithBuiltTree(const string in_file_name, const string out_file_name) throw (string);
	void WriteCode(const string in_file_name, const string out_file_name) throw (string);
	void WriteBuiltTree(const string hftree_name);
	void WriteCodeWithTree(const string hftree_name, const string in_file_name, const string out_file_name) throw (string);
	void ReadWithTree(const string hftree_name, const string in_file_name, const string out_file_name) throw (string);
	void ReadWithBuiltTree(const string in_file_name, const string out_file_name) throw (string);
	void Decompress(const string in_file_name, const string out_file_name);
	void PrintTree(string out_file_name) throw (string);
	void setSize(int size) { this->size = size; }
private:
	int size;
	ifstream in_file;
	ofstream out_file;
	string filename;
	unsigned long in_file_size;
	string *table;
	//map<int, string> dictionary;
	//map<int, string>::iterator iter;
	HFNode<T>* root;
	void EncodeTranversal();
	void CloseFile();
	void Code();
	void BuildTree();
	void OutputHFTree();
	void Deactivate();
	void InOrderVisit(HFNode<T>* node, unsigned long pos, string * list);
	void Decode() throw (string);
	void RebuildBy(const string hftree_name);
	void RebuildTree() throw (string);
};

template <class T>
void Huffman<T>::CloseFile()
{
	in_file.close();
	out_file.close();
}

template <class T>
bool Huffman<T>::TreeIsBuilt()
{
	return (root != NULL);
}

template <class T>
void Huffman<T>::BuildTree()
{
	int i, count = 0;
	T freq[MAX_SIZE] = { 0 };
	char in_char;

	in_file.clear();
	in_file.seekg(ios::beg);
	// ���ζ����ַ���ͳ������
	while (!in_file.eof())
	{
		in_file.get(in_char);
		// ����������Ľ�����
		if (in_file.eof())
			break;
		// char���з��ŵģ������±���unsigned ����Ҫ����unsigned char
		freq[(unsigned char)in_char]++;
	}

	HFNode<T> *node = new HFNode<T>[MAX_SIZE];
	for (i = 0; i < MAX_SIZE; i++)
		if (freq[i] != 0)
			node[count++] = HFNode<T>(i, freq[i]);

	// ����Ƶ��Ϊ1��pseudo-EOF
	node[count++] = HFNode<T>(PSEUDO_EOF, 1);
	size = count;

	Initialize(node, size);
}

template <class T>
void Huffman<T>::Initialize(HFNode<T> node[], int n)
{
	// ��������һ����С��
	MinHeap<HFNode<T>> H(1);
	H.Initialize(node, n, n);
	//����ȡ����С��������Ȼ�󽫺ϲ��Ľ������
	HFNode<T> *x, *y, *z = new HFNode<T>;
	for (int i = 1; i < n; i++) 
	{
		x = new HFNode<T>;
		y = new HFNode<T>;
		H.DeleteMin(*x);
		H.DeleteMin(*y);
		z = new HFNode<T>(-1, x->weight + y->weight, x, y);
		H.Insert(*z);
	}
	this->root = z;
	delete[]node;
	EncodeTranversal();
}

template<class T>
void Huffman<T>::EncodeTranversal()
{
	//dictionary.clear();
	table = new string[MAX_SIZE];
	//��α���������ڵ�����룬�������ⲿ�ڵ��mapӳ��
	queue<HFNode<T>*> q;
	if (root == NULL)
		return;
	q.push(root);
	while (!q.empty())
	{
		for (int i = 0; i < q.size(); i++)
		{
			HFNode<T>* p = q.front();
			q.pop();
			if (p->LeftChild)
			{
				q.push(p->LeftChild);
				p->LeftChild->code = p->code + '0';
			}
				
			if (p->RightChild)
			{
				q.push(p->RightChild);
				p->RightChild->code = p->code + '1';
			}

			if (p->id != -1)
				//dictionary.insert(pair<int, string>(p->id, p->code));
				table[p->id] = p->code;
		}
	}
}

template<class T>
void Huffman<T>::SetInPath(const string in_file_name)
{
	in_file.close();
	filename = in_file_name.substr(in_file_name.find_last_of('\\') + 1);
	filename = filename.substr(in_file_name.find_last_of('/') + 1);
	in_file.open(in_file_name, ios_base::in | ios_base::binary);
	if (!in_file.is_open())
		throw string("�����ļ���ʧ��");
	in_file.seekg(0, ios::end);
	in_file_size = in_file.tellg();
	in_file.seekg(ios::beg);
}

template<class T>
void Huffman<T>::SetOutPath(const string out_file_name)
{
	out_file.close();
	out_file.open(out_file_name, ios_base::out | ios_base::binary);
}

template<class T>
void Huffman<T>::WriteCodeWithBuiltTree(const string in_file_name, const string out_file_name)
{
	try
	{
		if (root == NULL)
			throw string("δ��ʼ��huffman��");
		SetInPath(in_file_name);
		SetOutPath(out_file_name);
		Code(); 
		CloseFile();
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::WriteCode(const string in_file_name, const string out_file_name)
{
	try
	{
		SetInPath(in_file_name);
		BuildTree();
		SetOutPath(out_file_name);
		Code();
		CloseFile();
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::WriteBuiltTree(const string hftree_name)
{
	SetOutPath(hftree_name);
	OutputHFTree();
	CloseFile();
}

template<class T>
void Huffman<T>::WriteCodeWithTree(const string hftree_name, const string in_file_name, const string out_file_name)
{
	try
	{
		RebuildBy(hftree_name);
		SetInPath(in_file_name);
		SetOutPath(out_file_name);
		Code();
		CloseFile();
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::Compress(const string in_file_name, const string out_file_name)
{
	try
	{
		std::ios::sync_with_stdio(false);  
		std::cin.tie(0);
		SetInPath(in_file_name);
		BuildTree();
		SetOutPath(out_file_name);
		//��д�ļ������ļ���
		out_file << "0001";
		out_file << (char)(filename.length() / 256) << (char)(filename.length() % 256) << filename;
		OutputHFTree();
		Code();
		CloseFile();
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::Code()
{
	int length, i, j;
	long read_num, pos = 0;
	char in_char;
	char read_buf[READ_BUFF_SIZE];
	unsigned char out_c = 0, tmp_c = 1;
	string code_string;
	string out_string;
	unsigned long now = 0;

	// д��huffman����
	in_file.clear();
	in_file.seekg(ios::beg);

	//char buf[1024*8];
	//in_file.rdbuf()->pubsetbuf(buf, sizeof buf);

	while (!in_file.eof())
	{
		//in_file.get(in_char);
		// ���⽫�������д��
		if (in_file.eof())
			break;
		in_file.read(read_buf, READ_BUFF_SIZE);
		read_num = in_file.gcount();

		for (pos = 0; pos < read_num; pos++)
		{
			in_char = read_buf[pos];
			now++;	

			// �ҵ�ÿһ���ַ�����Ӧ��huffman����
			code_string += table[(unsigned char)in_char];
			//iter = dictionary.find((unsigned char)in_char);
			//if (iter != dictionary.end())
			//	code_string += iter->second;
			//else
			//{
			//	cout << "Can't find the huffman code of character" + in_char << endl;
			//	throw;
			//}

			// ���ܱ���ĳ��ȴ���Ԥ���WRITE_BUFF_SIZEʱ��д���ļ�
			length = code_string.length();
			if (length > CODE_BUFF_SIZE)
			{
				out_string.clear();

				//ȷ��out_string��û�вд��char
				for (i = 0; i + 7 < length; i += 8)
				{
					// ÿ��λ01ת����һ��unsigned char���
					// ��ʹ��char�����ʹ��char������λ������ʱ�����λ��Ӱ����
					// ����char��unsigned char�໥ת��������λ������
					out_c = 0;
					for (j = 0; j < 8; j++)
					{
						if ('1' == code_string[i + j])
							out_c += tmp_c << (7 - j);
					}
					out_string += out_c;
				}
				out_file << out_string;
				code_string = code_string.substr(i, length - i);

				int progress = (int)(100 * now / in_file_size);
				cout << "������......[" << progress << "%]\r";
			}
		}
	}

	// �Ѷ��������ļ����Ȳ���pseudo-EOF
	code_string += table[PSEUDO_EOF];

	//iter = dictionary.find(PSEUDO_EOF);
	//if (iter != dictionary.end())
	//	code_string += iter->second;
	//else
	//{
	//	cout << "Can't find the huffman code of pseudo-EOF" << endl;
	//	throw;
	//}

	// �ٴ���β��ʣ���huffman����
	length = code_string.length();
	out_c = 0;
	for (i = 0; i < length; i++)
	{
		if ('1' == code_string[i])
			out_c += tmp_c << (7 - (i % 8));
		if (0 == (i + 1) % 8 || i == length - 1)
		{
			// ÿ8λд��һ���ļ�
			out_file << out_c;
			out_c = 0;
		}
	}
	cout << "������......[100%]" << endl;
	out_file.close();
}

template<class T>
void Huffman<T>::OutputHFTree()
{
	if (root == NULL)
		throw;

	int count = 0;
	char* write_buf = new char[WRITE_BUFF_SIZE];
	// ��1��2�ֽ�д��ڵ�����byte��
	write_buf[count++] = (char)(size / 256);
	write_buf[count++] = (char)(size % 256);
	char out_c = 0, tmp_c = 1;
	// ������д��huffman����ÿ��д���ַ�+���볤��+huffman����Ķ�����(��λ0���룩
	for (int i = 0; i < MAX_SIZE; i++)
		if (table[i] != "")
		{
			int length = table[i].length();
			if (i != PSEUDO_EOF)
				write_buf[count++] = (char)i;
			write_buf[count++] = (char)length;
			for (int j = 0; j < length; j++) {
				if ('1' == table[i][j])
					out_c += tmp_c << (7 - (j % 8));
				if (0 == (j + 1) % 8 || j == length - 1) {
					// ÿ8λд��һ���ļ�
					write_buf[count++] = out_c;
					out_c = 0;
				}
			}
		}
	out_file.write(write_buf, count);

}

template<class T>
void Huffman<T>::ReadWithTree(const string hftree_name, const string in_file_name, const string out_file_name)
{
	try
	{
		RebuildBy(hftree_name);
		ReadWithBuiltTree(in_file_name, out_file_name);
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::ReadWithBuiltTree(const string in_file_name, const string out_file_name)
{
	try
	{
		SetInPath(in_file_name);
		SetOutPath(out_file_name);
		Decode();
		CloseFile();
	}
	catch (string s)
	{
		throw;
	}
}


template<class T>
void Huffman<T>::Decompress(const string in_file_name, const string out_file_name)
{
	try
	{
		SetInPath(in_file_name);
		int file_num = 0;
		char in_char;
		for (int i = 0; i < 4; i++)
		{
			in_file.get(in_char);
			file_num = file_num << 8;
			file_num += in_char;
		}
		string *dic = new string[file_num];
		for (int i = 0; i < file_num; i++)
		{
			in_file.get(in_char);
			int len = 256 * int(in_char & 0xff);
			in_file.get(in_char);
			len += int(in_char & 0xff);
			char* name = new char[len];
			in_file.get(name, len+1);
			dic[i] = string(name);
		}
		for (int i = 0; i < file_num; i++)
		{
			RebuildTree();
			SetOutPath(out_file_name + "\\" + dic[i]);
			Decode();
		}
		CloseFile();
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::Decode()
{
	bool pseudo_eof;
	int i, id;
	long pos = 0, read_num;
	char in_char;
	char read_buf[READ_BUFF_SIZE];
	string out_string;
	unsigned char u_char, flag;
	HFNode<T>* node;

	out_string.clear();
	node = root;
	pseudo_eof = false;

	unsigned long now = 0;

	while (!in_file.eof())
	{
		//in_file.get(in_char);

		// ���⽫�������д��
		if (in_file.eof())
			break;
		in_file.read(read_buf, READ_BUFF_SIZE);
		read_num = in_file.gcount();

		for (pos = 0; pos < read_num; pos++)
		{
			in_char = read_buf[pos];
			now++;

			u_char = (unsigned char)in_char;
			flag = 0x80;
			for (i = 0; i < 8; ++i)
			{
				//&�ǰ�λ��
				if (u_char & flag)
					node = node->RightChild;
				else
					node = node->LeftChild;

				id = node->id;
				if (id >= 0)
				{
					if (id == PSEUDO_EOF)
					{
						pseudo_eof = true;
						break;
					}
					else
					{
						// int to char�ǰ�ȫ�ģ���λ�ᱻ�ض�
						out_string += (char)id;
						node = root;
					}
				}
				flag = flag >> 1;
			}
			if (pseudo_eof)
				break;

			if (CODE_BUFF_SIZE < out_string.length())
			{
				out_file << out_string;
				out_string.clear();
				int progress = (int)(100 * now / in_file_size);
				cout << "������......[" << progress << "%]\r";
			}
		}
	}

	if (!out_string.empty())
		out_file << out_string;
	cout << "������......[100%]\n";
	out_file.close();
}

template<class T>
void Huffman<T>::RebuildBy(const string hftree_name)
{
	try
	{
		SetInPath(hftree_name);
		RebuildTree();
	}
	catch (string s)
	{
		throw;
	}
}

template<class T>
void Huffman<T>::RebuildTree()
{
	int i, j, id, length;
	char in_char;
	unsigned char u_char, flag = 0x80;
	string code;
	HFNode<T> *node;
	Deactivate();
	root = new HFNode<T>();

	in_file.get(in_char);
	size = 256 * int(in_char & 0xff);
	in_file.get(in_char);
	size += int(in_char & 0xff);
	if (size > MAX_SIZE)
	{
		throw string("�ļ�ͷ�ڵ�������ȷ���ļ�������");
	}

	for (i = 0; i < size; ++i)
	{
		node = root;
		code = "";
		if (i == size - 1)
			id = PSEUDO_EOF;
		else
		{
			in_file.get(in_char);
			id = in_char & 0xff;
		}
		in_file.get(in_char);
		length = in_char & 0xff;
		for (j = 0; j < length; j++)
		{
			if (j % 8 == 0)
			{
				in_file.get(in_char);
				u_char = (unsigned char)in_char;
				flag = 0x80;
			}

			if (node->id != -1)
			{
				//;�з��ʵ�����Ҷ�ӽڵ㣬˵��Ѱ·���������ַ��ı��봦��huffman���벻��
				throw string("�ļ�ͷ�����ʽ����ȷ���ļ���.");
			}
			if ((u_char & flag) == 0)
			{
				code += "0";
				if (node->LeftChild == NULL)
					node->LeftChild = new HFNode<T>();
				else if (j == length - 1)
				{
					throw string("�ļ�ͷ�����ʽ����ȷ���ļ���.");
				}
				node = node->LeftChild;
			}
			else
			{
				code += "1";
				if (node->RightChild == NULL)
					node->RightChild = new HFNode<T>();
				else if (j == length - 1)
				{
					throw string("�ļ�ͷ�����ʽ����ȷ���ļ���.");
				}
				node = node->RightChild;
			}
			flag = flag >> 1;
		}
		node->id = id;
		node->code = code;
	}
	EncodeTranversal();
}

template<class T>
void Huffman<T>::Deactivate()
{
	//��α����ͷ��ڴ�
	queue<HFNode<T>*> q;
	if (root == NULL)
		return;
	q.push(root);
	while (!q.empty())
	{
		for (int i = 0; i < q.size(); i++)
		{
			HFNode<T>* p = q.front();
			q.pop();
			if (p->LeftChild)
				q.push(p->LeftChild);
			if (p->RightChild)
				q.push(p->RightChild);
			if (p)
				delete p;
		}
	}
}

template <class T>
void Huffman<T>::InOrderVisit(HFNode<T>* node, unsigned long pos, string* list)
{
	if (node == NULL)
		return;
	InOrderVisit(node->LeftChild, pos * 2, list);
	if (node->id != -1)
		list[pos] = std::to_string(node->id);
	else
		list[pos] = "XXX";
	InOrderVisit(node->RightChild, pos * 2 + 1, list);
}

template <class T>
void Huffman<T>::PrintTree(string out_file_name)
{
	if (root == NULL)
		throw string("Huffman��δ��ʼ��");

	SetOutPath(out_file_name);

	int height = 0;
	//for (iter = dictionary.begin(); iter != dictionary.end(); iter++)
	//	if (height < iter->second.length())
	//		height = iter->second.length();
	for(int i = 0; i < MAX_SIZE; i++)
		if (height < table[i].length())
			height = table[i].length();
	height++;

	int i, j, k ,l;
	string str;
	int max_size = pow(2, height);
	string *list = new string[max_size];
	for (i = 0; i < max_size; i++)
		list[i] = "   ";
	list[1] = "XXX";

	InOrderVisit(root, 1, list);

	int s = pow(2, height) - 1;	
	int h = height;
	for (i = 1; i <= s; i++)
	{
		for (j = 0; j < pow(2, h - 1) - 1; j++)
			out_file << "  ";
		out_file << setw(3) << setfill('0') << list[i];
		for (j = 0; j < pow(2, h - 1) - 1; j++)
			out_file << "  ";
		out_file << ' ';
		if (i == pow(2, height - h + 1) - 1)
		{
			out_file << endl;
			if (i == s)
				break;
			for (k = i+1; k <= 2 * i + 1; k++)
			{
				if (list[k] == "   ")
					for (l = 0; l < pow(2, h) - 1; l++)
						out_file << ' ';
				else
				{
					if (k % 2 == 0)
					{
						for (l = 0; l < pow(2, h - 1); l++)
							out_file << ' ';
						for (l = 0; l < pow(2, h - 1) - 2; l++)
							out_file << '_';
						out_file << '/';
					}
					else
					{
						out_file << '\\';
						for (l = 0; l < pow(2, h - 1) - 2; l++)
							out_file << '_';
						for (l = 0; l < pow(2, h - 1); l++)
							out_file << ' ';
					}
				}
				out_file << ' ';
			}

			out_file << endl;

			for (k = i + 1; k <= 2 * i + 1; k++)
			{
				if (list[k] == "   ")
					for (l = 0; l < pow(2, h) - 1; l++)
						out_file << ' ';
				else
				{
					if (k % 2 == 0)
					{
						for (l = 0; l < pow(2, h - 1) - 1; l++)
							out_file << ' ';
						out_file << '/';
						for (l = 0; l < pow(2, h - 1) - 1; l++)
							out_file << ' ';
					}
					else
					{
						for (l = 0; l < pow(2, h - 1) - 1; l++)
							out_file << ' ';
						out_file << '\\';
						for (l = 0; l < pow(2, h - 1) - 1; l++)
							out_file << ' ';
					}
				}
				out_file << ' ';
			}
			out_file << endl;
			h--;
		}
	}
	CloseFile();
}
