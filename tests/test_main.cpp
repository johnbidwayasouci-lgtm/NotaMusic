#include <lyria/editor/editor.hpp>
#include <lyria/midi/midi.hpp>
#include <lyria/music/music.hpp>
#include <lyria/notation/notation.hpp>
#include <cassert>
#include <memory>
int main(){auto s=lyria::music::make_satb_score("test");assert(s.staves.size()==4);lyria::editor::CommandHistory h;h.execute(std::make_unique<lyria::editor::SetTempo>(90),s);assert(s.tempo==90);assert(h.undo(s)&&s.tempo==120);assert(h.redo(s)&&s.tempo==90);assert(lyria::notation::layout_score(s).glyphs.empty());assert(lyria::midi::render(s).empty());return 0;}
