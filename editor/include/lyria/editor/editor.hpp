#pragma once
#include <lyria/music/music.hpp>
#include <memory>
#include <vector>
namespace lyria::editor { class Command{public:virtual~Command()=default;virtual void execute(music::Score&)=0;virtual void undo(music::Score&)=0;}; class SetTempo final:public Command{int before_{};int after_;public:explicit SetTempo(int t):after_(t){}void execute(music::Score&s)override{before_=s.tempo;s.tempo=after_;}void undo(music::Score&s)override{s.tempo=before_;}}; class CommandHistory{std::vector<std::unique_ptr<Command>>done_,undone_;public:void execute(std::unique_ptr<Command>,music::Score&);bool undo(music::Score&);bool redo(music::Score&);}; }
