#pragma once
#include <lyria/vocal/vocal.hpp>
namespace lyria::choir {struct ChoirConfiguration{int sopranos{5};int altos{5};int tenors{4};int basses{4};float stereoWidth{.8f};};class ChoirEngine{vocal::VocalSynthesisEngine&vocal_;public:explicit ChoirEngine(vocal::VocalSynthesisEngine&v):vocal_(v){}audio::Buffer render(const vocal::VocalRequest&,const ChoirConfiguration&)const;};}
