#pragma once
#include <lyria/music/music.hpp>
#include <string>
namespace lyria::project {struct ProjectMetadata{int formatMajor{1};int formatMinor{0};std::string name;};struct Project{ProjectMetadata metadata;music::Score score;};}
