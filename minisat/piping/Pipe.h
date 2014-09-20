#ifndef Pipe_h
#define Pipe_h

#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <string>

using namespace std;

class Pipe {
    public:
        static Pipe* getInstance();
        Pipe();
        ~Pipe();
        void openPipe(const string& filename);
        void writeToPipe(const string& output);
        
    private:
        static Pipe* instance;
        string filename;
        int pipe;
};

#endif
