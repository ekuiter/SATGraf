#include "Pipe.h"

Pipe* Pipe::instance = 0;

Pipe* Pipe::getInstance() {
    if (!instance) {
        instance = new Pipe();
    }
    
    return instance;
}

Pipe::Pipe() {
}

Pipe::~Pipe() {
    this->pipe = 0;
    this->instance = 0;
}

void Pipe::openPipe(const string& filename) {
    this->filename = filename;
    this->pipe = open(filename.c_str(), O_WRONLY);
}

void Pipe::writeToPipe(const string& output) {
    write(this->pipe, output.c_str(), output.length());
}
