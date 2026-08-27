#include <lyria/interpretation/interpretation.hpp>
namespace lyria::interpretation {NotePerformance BasicInterpreter::interpret(const music::Note&n)const{NotePerformance p;switch(n.dynamic){case music::Dynamic::P:case music::Dynamic::Pp:case music::Dynamic::Ppp:p.gain=.65f;break;case music::Dynamic::F:case music::Dynamic::Ff:case music::Dynamic::Fff:p.gain=1.15f;break;default:break;}return p;}}
