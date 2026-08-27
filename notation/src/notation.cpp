#include <lyria/notation/notation.hpp>
namespace lyria::notation { Layout layout_score(const music::Score&s){Layout l;float y=0;for(auto const&st:s.staves){float x=0;for(auto const&m:st.measures)for(auto const&n:m.notes){l.glyphs.push_back({n.id,x,y});x+=20;}y+=60;}return l;} }
