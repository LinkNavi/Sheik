#include "Clicker.h"
#include <cstdlib>
#include <cstring>

Clicker::Clicker() : ei_(nullptr), seat_(nullptr), dev_(nullptr) {
    ei_ = ei_new(nullptr);
    if (!ei_) return;

    ei_configure_name(ei_, "sheik-autoclicker");
    ei_setup_backend_socket(ei_, nullptr);

    for (int i = 0; i < 64 && !dev_; i++) {
        ei_dispatch(ei_);
        ei_event* ev;
        while ((ev = ei_get_event(ei_)) != nullptr) {
            switch (ei_event_get_type(ev)) {
                case EI_EVENT_SEAT_ADDED:
                    seat_ = ei_seat_ref(ei_event_get_seat(ev));
                    break;
                case EI_EVENT_DEVICE_ADDED:
                    dev_ = ei_device_ref(ei_event_get_device(ev));
                    break;
                default: break;
            }
            ei_event_unref(ev);
        }
    }
}

Clicker::~Clicker() {
    if (dev_)  ei_device_unref(dev_);
    if (seat_) ei_seat_unref(seat_);
    if (ei_)   ei_unref(ei_);
}

int Clicker::randJitter(int min, int max) {
    if (min >= max) return min;
    return min + (rand() % (max - min + 1));
}

void Clicker::leftClick() {
    if (!dev_) return;
    ei_device_button_button(dev_, 0x110, true);
    ei_device_frame(dev_, 0);
    ei_device_button_button(dev_, 0x110, false);
    ei_device_frame(dev_, 0);
    ei_dispatch(ei_);
}

void Clicker::rightClick() {
    if (!dev_) return;
    ei_device_button_button(dev_, 0x111, true);
    ei_device_frame(dev_, 0);
    ei_device_button_button(dev_, 0x111, false);
    ei_device_frame(dev_, 0);
    ei_dispatch(ei_);
}
