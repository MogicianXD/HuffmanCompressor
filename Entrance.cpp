#include "Huffman.h"
#include <iostream>
#include <iomanip>
#include <ctime>
#include <stdlib.h>

using namespace std;

string in_file_name, out_file_name, hftree_name;
Huffman<unsigned long> hf;
clock_t start;

void set_in_path()
{
	cout << "***输入要读取的文件路径或文件名**" << endl;
	cin >> in_file_name;
	in_file_name = in_file_name;
}

void set_out_path()
{
	cout << "***输入要写入的路径或文件名****" << endl;
	cin >> out_file_name;
	out_file_name = out_file_name;
}

void set_hfmtree_path()
{
	cout << "***输入霍夫曼树文件路径***" << endl;
	cin >> hftree_name;
}

void finish()
{
	cout << "成功！" << endl;
	clock_t end = clock();
	cout << "用时" << (double)(end - start) / CLOCKS_PER_SEC << "秒" << endl;
}

void OutputHFTree()
{
	cout << "Tip:是否将该树输出成hfmtree文件？" << endl;
	cout << "Tip:是，则输入1" << endl;
	cout << "Tip:否，则输入0" << endl;
	cout << "请输入： ";
	char flag;
	while (true)
	{
		cin >> flag;
		if (flag == '0' || flag == '1')
			break;
		cout << "输入格式不正确" << endl;
		cout << "请重新输入： ";
	}
	if (flag == '0')
		return;
	else
	{
		try
		{
			set_hfmtree_path();
			start = clock();
			hf.WriteBuiltTree(hftree_name);
			finish();
		}
		catch (string s)
		{
			throw;
		}
	}
}

void Initialize()
{
	cout << "***请输入字符数***" << endl;
	int size;
	cin >> size;
	char x;
	long weight = 0;
	HFNode<unsigned long>* node = new HFNode<unsigned long>[size];
	for (int i = 0; i < size; i++)
	{
		cin >> x >> weight;
		node[i] = HFNode<unsigned long>((int)x, weight);
	}
	try
	{
		start = clock();
		hf.Initialize(node, size);
		finish();
		hf.setSize(size);
		OutputHFTree();
	}
	catch (string s)
	{
		throw;
	}
}

void Encoding()
{
	cout << "Tip:输入0,则根据待编码文件重建" << endl;
	cout << "Tip:输入1,再输入hfmtree文件，则根据该hfmtree重建" << endl;
	cout << "请输入： ";
	char flag;
	while (true)
	{
		cin >> flag;
		if (flag == '0' || flag == '1')
			break;
		cout << "输入格式不正确" << endl;
		cout << "请重新输入： ";
	}
	try
	{

		if (flag == '1')
		{
			set_hfmtree_path();
			set_in_path();
			set_out_path();
			start = clock();
			hf.WriteCodeWithTree(hftree_name, in_file_name, out_file_name);
			finish();
		}
		else
		{
			set_in_path();
			set_out_path();
			start = clock();
			hf.WriteCode(in_file_name, out_file_name);
			finish();
			OutputHFTree();
		}
	}
	catch (string s)
	{
		throw;
	}
}

void Coding()
{
	try
	{
		if (hf.TreeIsBuilt())
		{
			set_in_path();
			set_out_path();
			start = clock();
			hf.WriteCodeWithBuiltTree(in_file_name, out_file_name);
			finish();
			OutputHFTree();
		}
		else
		{
			cout << "Err:Huffman树不在内存中，需要重建" << endl;
			Encoding();
		}
	}
	catch (string s)
	{
		throw;
	}
}

void Decompress()
{
	try
	{
		set_in_path();	
		cout << "***输入要压缩到的文件夹路径****" << endl;
		cin >> out_file_name;
		out_file_name = out_file_name;
		start = clock();
		hf.Decompress(in_file_name, out_file_name);
		finish();
	}
	catch (string s)
	{
		throw;
	}
}

void DeEncoding()
{
	try
	{
		cout << "输入hfmtree文件，则根据该hfmtree重建" << endl;
		cout << "请输入： ";
		set_hfmtree_path();
		set_in_path();
		set_out_path();
		start = clock();
		hf.ReadWithTree(hftree_name, in_file_name, out_file_name);
		finish();
	}
	catch (string s)
	{
		throw;
	}
}

void Decoding()
{
	try
	{
		if (hf.TreeIsBuilt())
		{
			set_in_path();
			set_out_path();
			start = clock();
			hf.ReadWithBuiltTree(in_file_name, out_file_name);
			finish();
		}
		else
		{
			cout << "Err:Huffman树不在内存中，需要重建" << endl;
			DeEncoding();
		}
	}
	catch (string)
	{
		throw;
	}

}

void Compress()
{
	try
	{
		set_in_path();
		set_out_path();
		start = clock();
		for(int i = 0; i < 10; i++)
		hf.Compress(in_file_name, out_file_name);
		finish();
	}
	catch (string s)
	{
		throw;
	}
}

void PrintCode()
{
	try
	{
		set_in_path();
		start = clock();
		ifstream in_file(in_file_name);
		int count = 0;

		//中文会出现乱码，但题目只是打印译码也无所谓了
		char in_char;
		string str;
		cout << "译码文件打印如下：" << endl;
		while (!in_file.eof())
		{
			in_char = in_file.get();
			str += in_char;
			count++;
			if (in_file.eof())
				break;
			else if (in_char == '\t')
				count = (count / 8 + 1) * 8;
			else if (in_char == '\n')
			{
				in_char = in_file.get();
				str += in_char;
				cout << str;
				if (in_char != '\r')
					count = 1;
				else
					count = 0;
				str = "";
			}
			else if (count >= 50)
			{
				cout << str << endl;
				count = 0;
				str = "";
			}
		}
		in_file.close();
		finish();
	}
	catch (string)
	{
		throw;
	}
}

void PrintTree()
{
	try
	{
		set_out_path();
		hf.PrintTree(out_file_name);
	}
	catch (string)
	{
		throw;
	}
}

int main()
{
	cout << "======================================" << endl;
	cout << "      欢迎使用霍夫曼编码解码系统" << endl;
	cout << "======================================" << endl;
	cout << "(0)给定字符与权重初始化Huffman请输入'i'" << endl;
	cout << "(1)以内存中Huffman树编码请输入'c'" << endl;
	cout << "(2)指定文件构建Huffman树编码请输入'e'" << endl;
	cout << "(3)以内存中Huffman树解码请输入'r'" << endl;
	cout << "(4)指定文件构建Huffman树解码请输入'm'" << endl;
	cout << "(5)打印编码请输入'p'" << endl;
	cout << "(6)打印赫夫曼树请输入't'" << endl;
	cout << "(7)压缩请输入'z'" << endl;
	cout << "(8)解压请输入'd'" << endl;
	cout << "(9)离开请输入'q'" << endl;
	cout << "======================================" << endl;
	char choice = 'a';
	while (choice != 'q')
	{
		cout << "请输入下一步： ";
		cin >> choice;
		try
		{
			switch (choice)
			{
			case 'i':
				Initialize();
				break;
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
				cout << "input error" << endl;
				continue;
			}
		}
		catch(string s)
		{
			cout << "失败！" << endl;
			cout << s << endl;
			continue;
		}
	}
	return 0;
}