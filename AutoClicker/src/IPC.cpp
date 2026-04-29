#include "IPC.h"
#include <cstring>

SheikIPC::SheikIPC(const char* shmPath) {
    fd_ = open(shmPath, O_RDONLY);
    if (fd_ < 0) {
        perror("open");
        return;
    }

    mapped_ = mmap(nullptr, size_, PROT_READ, MAP_SHARED, fd_, 0);
    if (mapped_ == MAP_FAILED) {
        perror("mmap");
        close(fd_);
        fd_ = -1;
        return;
    }

    close(fd_);  // fd not needed after mmap
    fd_ = -1;
    state_ = static_cast<GameState*>(mapped_);
}

SheikIPC::~SheikIPC() {
    if (mapped_ && mapped_ != MAP_FAILED) {
        munmap(mapped_, size_);
    }
    if (fd_ >= 0) {
        close(fd_);
    }
}

void SheikIPC::update() {
    if (!state_) return;
    __sync_synchronize();  // Memory barrier
}
