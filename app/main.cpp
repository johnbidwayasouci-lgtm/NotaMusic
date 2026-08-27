#include <lyria/ai/ai.hpp>
#include <lyria/choir/choir.hpp>
#include <lyria/midi/midi.hpp>
#include <lyria/music/music.hpp>
#include <lyria/notation/notation.hpp>
#include <lyria/vocal/vocal.hpp>
#include <iostream>
int main(){auto s=lyria::music::make_satb_score("LyriaScrib IA development");auto l=lyria::notation::layout_score(s);auto m=lyria::midi::render(s);lyria::vocal::SimulatedVocalEngine v;lyria::choir::ChoirEngine c(v);lyria::vocal::VocalRequest r;auto a=c.render(r,{});lyria::ai::NoOpAnalysisProvider ai;std::cout<<"LyriaScrib IA development foundation\n"<<"staves: "<<s.staves.size()<<"\n"<<"notation glyphs: "<<l.glyphs.size()<<"\n"<<"MIDI events: "<<m.size()<<"\n"<<"simulated audio samples: "<<a.samples.size()<<"\n"<<"AI suggestions: "<<ai.analyze(s).size()<<"\n";}
