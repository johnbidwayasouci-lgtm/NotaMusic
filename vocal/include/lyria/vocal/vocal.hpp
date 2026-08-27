#pragma once
#include <lyria/audio/audio.hpp>
#include <lyria/music/music.hpp>
#include <string>
#include <vector>
namespace lyria::vocal {struct VocalRequest{std::vector<std::string>phonemes;music::Pitch pitch{};double durationSeconds{1};float gain{1};};class VocalSynthesisEngine{public:virtual~VocalSynthesisEngine()=default;virtual audio::Buffer synthesize(const VocalRequest&)=0;virtual const char*name()const noexcept=0;};class SimulatedVocalEngine final:public VocalSynthesisEngine{public:audio::Buffer synthesize(const VocalRequest&)override;const char*name()const noexcept override{return "SimulatedVocalEngine";}};}
