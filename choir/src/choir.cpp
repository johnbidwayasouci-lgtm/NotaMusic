#include <lyria/choir/choir.hpp>
namespace lyria::choir {audio::Buffer ChoirEngine::render(const vocal::VocalRequest&r,const ChoirConfiguration&)const{return vocal_.synthesize(r);}}
