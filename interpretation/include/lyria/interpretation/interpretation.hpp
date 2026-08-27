#pragma once
#include <lyria/music/music.hpp>
namespace lyria::interpretation {struct NotePerformance{double startScale{1};double durationScale{1};float gain{1};};class Interpreter{public:virtual~Interpreter()=default;virtual NotePerformance interpret(const music::Note&)const=0;};class BasicInterpreter final:public Interpreter{public:NotePerformance interpret(const music::Note&)const override;};}
