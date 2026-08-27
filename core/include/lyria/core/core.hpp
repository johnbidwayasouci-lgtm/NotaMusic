#pragma once
#include <cstdint>
#include <string>
namespace lyria::core { using Id=std::uint64_t; struct IdGenerator{Id next{1};Id make()noexcept{return next++;}}; struct Version{int major{0};int minor{1};int patch{0};std::string str()const;}; }
