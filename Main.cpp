#include <sys/types.h>
#include <stdio.h>
#include <io.h>
#include <process.h>

using namespace std;

int value = 5;
int main()
{
	pid_t pid;
	pid = fork();
	if (pid == 0)
		value += 15;
	else if (pid > 0)
	{
		wait(NULL);
		printf("%d", value);
		exit();
	}
}