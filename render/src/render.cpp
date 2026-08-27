#include <lyria/render/render.hpp>
namespace lyria::render {audio::Buffer Pipeline::preview(const vocal::VocalRequest&r,const choir::ChoirConfiguration&c)const{choir::ChoirEngine e(vocal_);return e.render(r,c);}}
