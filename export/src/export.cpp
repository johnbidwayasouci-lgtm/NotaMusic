#include <lyria/export/export.hpp>
namespace lyria::exporter {std::string describe(const music::Score&s){return s.title+" ("+std::to_string(s.staves.size())+" staves)";}}
