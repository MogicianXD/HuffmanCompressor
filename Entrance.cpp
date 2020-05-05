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
	cout << "***����Ҫ��ȡ���ļ�·�����ļ���**" << endl;
	cin >> in_file_name;
	in_file_name = in_file_name;
}

void set_out_path()
{
	cout << "***����Ҫд���·�����ļ���****" << endl;
	cin >> out_file_name;
	out_file_name = out_file_name;
}

void set_hfmtree_path()
{
	cout << "***������������ļ�·��***" << endl;
	cin >> hftree_name;
}

void finish()
{
	cout << "�ɹ���" << endl;
	clock_t end = clock();
	cout << "��ʱ" << (double)(end - start) / CLOCKS_PER_SEC << "��" << endl;
}

void OutputHFTree()
{
	cout << "Tip:�Ƿ񽫸��������hfmtree�ļ���" << endl;
	cout << "Tip:�ǣ�������1" << endl;
	cout << "Tip:��������0" << endl;
	cout << "�����룺 ";
	char flag;
	while (true)
	{
		cin >> flag;
		if (flag == '0' || flag == '1')
			break;
		cout << "�����ʽ����ȷ" << endl;
		cout << "���������룺 ";
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
	cout << "***�������ַ���***" << endl;
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
	cout << "Tip:����0,����ݴ������ļ��ؽ�" << endl;
	cout << "Tip:����1,������hfmtree�ļ�������ݸ�hfmtree�ؽ�" << endl;
	cout << "�����룺 ";
	char flag;
	while (true)
	{
		cin >> flag;
		if (flag == '0' || flag == '1')
			break;
		cout << "�����ʽ����ȷ" << endl;
		cout << "���������룺 ";
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
			cout << "Err:Huffman�������ڴ��У���Ҫ�ؽ�" << endl;
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
		cout << "***����Ҫѹ�������ļ���·��****" << endl;
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
		cout << "����hfmtree�ļ�������ݸ�hfmtree�ؽ�" << endl;
		cout << "�����룺 ";
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
			cout << "Err:Huffman�������ڴ��У���Ҫ�ؽ�" << endl;
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

		//���Ļ�������룬����Ŀֻ�Ǵ�ӡ����Ҳ����ν��
		char in_char;
		string str;
		cout << "�����ļ���ӡ���£�" << endl;
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
	cout << "      ��ӭʹ�û������������ϵͳ" << endl;
	cout << "======================================" << endl;
	cout << "(0)�����ַ���Ȩ�س�ʼ��Huffman������'i'" << endl;
	cout << "(1)���ڴ���Huffman������������'c'" << endl;
	cout << "(2)ָ���ļ�����Huffman������������'e'" << endl;
	cout << "(3)���ڴ���Huffman������������'r'" << endl;
	cout << "(4)ָ���ļ�����Huffman������������'m'" << endl;
	cout << "(5)��ӡ����������'p'" << endl;
	cout << "(6)��ӡ�շ�����������'t'" << endl;
	cout << "(7)ѹ��������'z'" << endl;
	cout << "(8)��ѹ������'d'" << endl;
	cout << "(9)�뿪������'q'" << endl;
	cout << "======================================" << endl;
	char choice = 'a';
	while (choice != 'q')
	{
		cout << "��������һ���� ";
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
			cout << "ʧ�ܣ�" << endl;
			cout << s << endl;
			continue;
		}
	}
	return 0;
}