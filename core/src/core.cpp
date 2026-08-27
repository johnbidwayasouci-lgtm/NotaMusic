#include <lyria/core/core.hpp>
namespace lyria::core { std::string Version::str()const{return std::to_string(major)+"."+std::to_string(minor)+"."+std::to_string(patch);} }
