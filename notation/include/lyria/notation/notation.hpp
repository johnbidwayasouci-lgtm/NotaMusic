#pragma once
#include <lyria/music/music.hpp>
#include <vector>
namespace lyria::notation { struct Glyph{core::Id noteId{};float x{};float y{};}; struct Layout{std::vector<Glyph>glyphs;}; Layout layout_score(const music::Score&); }
