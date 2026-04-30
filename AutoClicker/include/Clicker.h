#pragma once
#include <cstdint>
#include <cstdlib>
#include <libei.h>

class Clicker {
public:
    Clicker();
    ~Clicker();
    void leftClick();
    void rightClick();
    int randJitter(int min, int max);
private:
    ei* ei_;
    ei_seat* seat_;
    ei_device* dev_;
};;
