#pragma once
#include <lyria/interpretation/interpretation.hpp>
#include <lyria/music/music.hpp>
#include <string>
#include <vector>
namespace lyria::ai {struct Suggestion{std::string text;};class AnalysisProvider{public:virtual~AnalysisProvider()=default;virtual std::vector<Suggestion>analyze(const music::Score&)=0;};class NoOpAnalysisProvider final:public AnalysisProvider{public:std::vector<Suggestion>analyze(const music::Score&)override{return {};}};}
