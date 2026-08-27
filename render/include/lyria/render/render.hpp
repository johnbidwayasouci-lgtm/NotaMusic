#pragma once
#include <lyria/choir/choir.hpp>
#include <lyria/interpretation/interpretation.hpp>
namespace lyria::render {class Pipeline{vocal::VocalSynthesisEngine&vocal_;interpretation::Interpreter&interpreter_;public:Pipeline(vocal::VocalSynthesisEngine&v,interpretation::Interpreter&i):vocal_(v),interpreter_(i){}audio::Buffer preview(const vocal::VocalRequest&,const choir::ChoirConfiguration&)const;};}
