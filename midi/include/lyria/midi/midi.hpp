#pragma once
#include <lyria/music/music.hpp>
#include <cstdint>
#include <vector>
namespace lyria::midi { struct Event{std::uint64_t tick{};bool on{};int note{};int velocity{};};std::vector<Event>render(const music::Score&); }
