import {motion} from 'framer-motion'

function FeatureCard({icon, title, description, index}) {
    return (
        <motion.div
            initial={{opacity: 0, y: 30}}
            whileInView={{opacity: 1, y: 0}}
            viewport={{once: true, margin: "-50px"}}
            transition={{
                duration: 0.6,
                delay: index * 0.15,
                ease: [0.25, 0.46, 0.45, 0.94]
            }}
            whileHover={{
                y: -8,
                transition: {duration: 0.3}
            }}
            className="group relative"
        >
            {/* Card Background with Glass Effect */}
            <div
                className="absolute inset-0 bg-gradient-to-br from-white/80 to-white/40 backdrop-blur-xl rounded-2xl border border-white/60 shadow-lg shadow-blue-900/5 transition-all duration-500 group-hover:shadow-xl group-hover:shadow-blue-900/10 group-hover:border-blue-200/80"/>

            {/* Animated Gradient Border */}
            <div
                className="absolute inset-0 rounded-2xl bg-gradient-to-br from-blue-400/0 via-blue-500/0 to-blue-600/0 opacity-0 group-hover:opacity-100 transition-opacity duration-500"
                style={{padding: '1px'}}>
                <div className="w-full h-full rounded-2xl bg-gradient-to-br from-white/90 to-white/70"/>
            </div>

            {/* Content */}
            <div className="relative p-8">
                {/* Icon Container */}
                <motion.div
                    className="w-14 h-14 mb-6 rounded-xl bg-gradient-to-br from-blue-500 to-blue-700 flex items-center justify-center text-white shadow-lg shadow-blue-500/30"
                    whileHover={{scale: 1.1, rotate: 5}}
                    transition={{type: "spring", stiffness: 400, damping: 10}}
                >
                    {icon}
                </motion.div>

                {/* Title */}
                <h3 className="text-xl font-bold text-slate-800 mb-3 tracking-tight">
                    {title}
                </h3>

                {/* Description */}
                <p className="text-slate-600 leading-relaxed text-sm">
                    {description}
                </p>
            </div>

            {/* Decorative Corner */}
            <div className="absolute top-0 right-0 w-20 h-20 overflow-hidden rounded-tr-2xl">
                <div
                    className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-blue-100/50 to-transparent transform translate-x-8 -translate-y-8 group-hover:translate-x-4 group-hover:-translate-y-4 transition-transform duration-500"/>
            </div>
        </motion.div>
    )
}

export default FeatureCard